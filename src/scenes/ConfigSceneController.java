package scenes;

import gameobjects.Farm;
import gameobjects.Player;
import gameobjects.Plot;
import gameobjects.items.Item;
import gameobjects.items.crops.Cauliflower;
import gameobjects.items.crops.Corn;
import gameobjects.items.crops.Crop;
import gameobjects.items.crops.Sunflower;
import gameobjects.items.tools.*;
import gameobjects.npc.FarmWorker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.Main;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;


public class ConfigSceneController {
    @FXML
    private Button buttonContinue;
    @FXML
    private TextField nameTextField;
    @FXML
    private ComboBox<String> seasonComboBox;
    @FXML
    private ComboBox<String> seedComboBox;
    @FXML
    private ComboBox<String> diffComboBox;
    @FXML
    private Label seedLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label diffLabel;
    @FXML
    private Label seasonLabel;
    @FXML
    private Button buttonLoad;

    @FXML
    public void nextScene(ActionEvent e) throws FileNotFoundException {
        // Show next scene
        Crop c = new Corn();
        if (nameTextField.getText() != null && !nameTextField.getText().equals("")) {
            Main.getPlayer().setName(nameTextField.getText());
            if (seedComboBox.getValue() != null) {
                if (seedComboBox.getValue().equals("Cauliflower")) {
                    c = new Cauliflower();
                } else if (seedComboBox.getValue().equals("Sunflower")) {
                    c = new Sunflower();
                }
            }
            Main.getPlayer().getInventory().add(c);
            if (seasonComboBox.getValue() != null) {
                Main.getPlayer().setSeason(seasonComboBox.getValue());
            }

            if (diffComboBox.getValue() != null) {
                Main.getPlayer().setDiff(diffComboBox.getValue());
                if ("Easy".equals(Main.getPlayer().getDiff())) {
                    Main.getPlayer().setMoney(200);
                } else if ("Hard".equals(Main.getPlayer().getDiff())) {
                    Main.getPlayer().setMoney(50);
                }
            }
            Main.getStage().setScene(FarmScene.getScene());
        } else {
            Alert nameAlert = new Alert(Alert.AlertType.ERROR);
            nameAlert.setHeaderText("Please enter a username to continue");
            nameAlert.setTitle("Invalid Username");
            nameAlert.show();
        }
    }

    @FXML
    public void load() {
        File dir = new File("src/saves");
        if (dir.listFiles().length == 0) {
            Alert noSaves = new Alert(Alert.AlertType.ERROR);
            noSaves.setHeaderText("No saves available");
            noSaves.show();
            return;
        }
        ObservableList<String> saves = FXCollections.observableArrayList();
        for (File f : dir.listFiles()) {
            saves.add(f.getName().substring(0, f.getName().length() - 4));
        }
        ChoiceDialog<String> chooseSave = new ChoiceDialog<>(saves.get(0), saves);
        chooseSave.setTitle("Select Save");
        chooseSave.setHeaderText("Please select a save ");
        Optional<String> save = chooseSave.showAndWait();
        save.ifPresent(s -> importDetails(s + ".txt"));
        Main.getStage().setScene(FarmScene.getScene());
    }

    private void importDetails(String fname) {
        Player player = Main.getPlayer();
        try (BufferedReader fsave = new BufferedReader(new FileReader("src/saves/" + fname))) {
            // Name
            player.setName(fsave.readLine());
            // Difficulty
            player.setDiff(fsave.readLine());
            // Money
            player.setMoney(Integer.parseInt(fsave.readLine()));
            // Day
            player.setDay(Integer.parseInt(fsave.readLine()));
            // Season
            player.setSeason(fsave.readLine());
            // Harvesting
            String[] harvesting = fsave.readLine().split(",");
            player.setHarvestingMaximum(Integer.parseInt(harvesting[0]));
            player.setHarvestingCount(Integer.parseInt(harvesting[1]));
            // Watering
            String[] watering = fsave.readLine().split(",");
            player.setWateringMaximum(Integer.parseInt(watering[0]));
            player.setWateringCount(Integer.parseInt(watering[1]));
            // Inventory
            ArrayList<Item> inventory = Main.getPlayer().getInventory();
            String[] items = fsave.readLine().split(",");
            for (String item : items) {
                if (item.equals("")) {
                    break;
                }
                inventory.add(whichItem(item.split(":")));
            }
            // Farm
            FarmScene.farmSceneInit();
            ArrayList<Plot> farm = FarmScene.getFarm();
            String[] plots = fsave.readLine().split(",");
            for (int i = 0; i < 25; i++) {
                String plot = plots[i];
                if (plot.equals("null")) {
                    continue;
                }
                String[] plotDetails = plot.split(":");
                Plot pl = farm.get(Integer.parseInt(plotDetails[0]));
                String[] cropDetails = plotDetails[1].split("/");
                Crop crop = whichCrop(cropDetails[0], Integer.parseInt(cropDetails[1]));
                pl.setCrop(crop);
                pl.setGrowth(Integer.parseInt(plotDetails[2]));
                pl.setWaterLevel(Integer.parseInt(plotDetails[3]));
                pl.setFertilizerLevel(Integer.parseInt(plotDetails[4]));
                if (plotDetails[5].contains("true")) {
                    pl.setWorker(new FarmWorker(Integer.parseInt(plotDetails[5].split("/")[1])));
                }
                if (plotDetails[6].equals("true")) {
                    pl.addPesticide();
                }
                pl.setPlotImage(pl.getCrop().getImage());
            }
            for (int i = 25; i < plots.length; i++) {
                String plot = plots[i];
                farm.add(new Plot());
                if (plot.equals("null")) {
                    continue;
                }
                String[] plotDetails = plot.split(":");
                Plot pl = farm.get(Integer.parseInt(plotDetails[0]));
                String[] cropDetails = plotDetails[1].split("/");
                Crop crop = whichCrop(cropDetails[0], Integer.parseInt(cropDetails[1]));
                crop.setLifeStage(Integer.parseInt(cropDetails[1]));
                pl.setCrop(crop);
                pl.setGrowth(Integer.parseInt(plotDetails[2]));
                pl.setWaterLevel(Integer.parseInt(plotDetails[3]));
                pl.setFertilizerLevel(Integer.parseInt(plotDetails[4]));
                if (plotDetails[5].contains("true")) {
                    pl.setWorker(new FarmWorker(Integer.parseInt(plotDetails[5].split("/")[1])));
                }
                if (plotDetails[6].equals("true")) {
                    pl.addPesticide();
                }
                pl.setPlotImage(pl.getCrop().getImage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FarmScene.setBuilt(true);
        }
    }

    private Crop whichCrop(String type, int lifestage) {
        if (type.equals("Corn")) {
            return new Corn(lifestage);
        } else if (type.equals("Cauliflower")) {
            return new Cauliflower(lifestage);
        } else if (type.equals("Sunflower")) {
            return new Sunflower(lifestage);
        } else {
            return null;
        }
    }


    private Item whichItem(String[] item) {
        if (item[0].equals("Shovel")) {
            return new Shovel(Integer.parseInt(item[1]));
        } else if (item[0].equals("Axe")) {
            return new Axe(Integer.parseInt(item[1]));
        } else if (item[0].equals("Sickle")) {
            return new Sickle(Integer.parseInt(item[1]));
        } else if (item[0].equals("Pesticide")) {
            return new Pesticide();
        } else if (item[0].equals("Fertilizer")) {
            return new Fertilizer();
        } else {
            return whichCrop(item[0], Integer.parseInt(item[1]));
        }
    }
}
