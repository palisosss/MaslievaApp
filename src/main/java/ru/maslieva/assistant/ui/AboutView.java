package ru.maslieva.assistant.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AboutView {
    private final VBox root = new VBox(16);

    public AboutView() {
        build();
    }

    public VBox getRoot() {
        return root;
    }

    private void build() {
        root.getStyleClass().add("content-area");

        Label title = new Label("О программе");
        title.getStyleClass().add("section-title");

        VBox card = new VBox(12);
        card.getStyleClass().add("about-card");

        card.getChildren().addAll(
                infoRow("Приложение:", "Ассистент преподавателя"),
                infoRow("Автор:", "Маслиева Полина Александровна"),
                infoRow("Группа:", "ИСп 22-1"),
                infoRow("Специальность:", "09.02.07 «Информационные системы и программирование»"),
                infoRow("Тема ВКР:", "Разработка интерактивного сервиса «Ассистент преподавателя» " +
                        "для автоматизированной проверки заданий по программированию"),
                new Label(),
                descriptionLabel()
        );

        root.getChildren().addAll(title, card);
    }

    private VBox infoRow(String label, String value) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("about-label");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("about-value");
        valueNode.setWrapText(true);
        VBox box = new VBox(2, labelNode, valueNode);
        box.setPadding(new Insets(0, 0, 4, 0));
        return box;
    }

    private Label descriptionLabel() {
        Label desc = new Label(
                "Настольное приложение предназначено для автоматизированной проверки домашних заданий " +
                "по программированию на языках Java и Haskell. Преподаватель создаёт задания с тестовыми " +
                "данными, загружает файлы решений студентов, запускает проверку и сохраняет результаты " +
                "в таблицу с возможностью экспорта в CSV."
        );
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #2c3e50; -fx-line-spacing: 2px;");
        return desc;
    }
}
