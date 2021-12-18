package net.marvk.fs.vatsim.map.view.preloader;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import net.marvk.fs.vatsim.map.api.VersionResponse;

import java.time.LocalDate;
import java.util.Base64;

public class PreloaderView implements FxmlView<PreloaderViewModel> {
    @FXML
    private Label header;

    @FXML
    private StackPane container;

    @FXML
    private Label versionAndName;

    @FXML
    private VBox taskHolder;

    @FXML
    private Label error;

    @FXML
    private HBox errorHolder;

    @FXML
    private Label close;

    @FXML
    private Label task;

    @FXML
    private ProgressBar progressBar;

    @InjectViewModel
    private PreloaderViewModel viewModel;

    public void initialize() {
        viewModel.progressPropertyWritable().bindBidirectional(progressBar.progressProperty());
        task.textProperty().bind(viewModel.taskDescriptionProperty());
        error.textProperty().bind(viewModel.errorProperty());

        taskHolder.getChildren().setAll(task);

        viewModel.errorProperty().addListener((observable, oldValue, newValue) -> {
            final boolean errorOccurred = newValue != null;
            close.setVisible(errorOccurred);

            if (errorOccurred) {
                taskHolder.getChildren().setAll(errorHolder);
            } else {
                taskHolder.getChildren().setAll(task);
            }
        });
        versionAndName.setText(viewModel.getVersionAndName());

        viewModel.versionResponseProperty().addListener((observable, oldValue, response) -> {
            if (response != null && response.getResult() == VersionResponse.Result.OUTDATED) {
                showNewVersionDialog(response);
            }
        });

        checkSpecial();
    }

    private void showNewVersionDialog(final VersionResponse response) {
        final ButtonType download = new ButtonType("Download");
        final ButtonType postpone = new ButtonType("Postpone");

        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(taskHolder.getScene().getWindow());
        alert.setTitle("New Version Available!");
        alert.setHeaderText("A new Version of VATprism (%s) is available.".formatted(response.getLatestVersion()));
        alert.setContentText("Would you like to download the latest version?");
        alert.getButtonTypes().setAll(download, postpone);
        final TextArea textArea = new TextArea(response.getChangelog());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(100);
        final VBox changelogBox = new VBox(new Label("Changelog:"), textArea);
        alert.getDialogPane().setExpandableContent(changelogBox);
        alert.getDialogPane().setExpanded(true);
        final Window window = alert.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(e -> window.hide());

        ((Button) alert.getDialogPane().lookupButton(download)).setDefaultButton(true);
        ((Button) alert.getDialogPane().lookupButton(postpone)).setDefaultButton(false);

        alert.showAndWait()
             .stream()
             .filter(e -> e == download)
             .findAny()
             .ifPresent(e -> viewModel.downloadNewVersion());
    }

    @FXML
    private void goToIssuePage(final MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            viewModel.goToIssuePage();
        }
    }

    @FXML
    public void exit() {
        viewModel.exit();
    }

    private void checkSpecial() {
        final LocalDate now = LocalDate.now();
        if (now.isEqual(LocalDate.of(now.getYear(), Integer.parseInt(decode("NA==")), Integer.parseInt(decode("MQ=="))))) {
            container.getStyleClass().add("container-special");
            header.setText(decode("Q0FUcHJpc20="));
            header.setStyle(decode("LWZ4LWZvbnQtZmFtaWx5OiAnQ29taWMgU2FucyBNUyc="));
        }
    }

    private static String decode(final String s) {
        return new String(Base64.getDecoder().decode(s));
    }
}
