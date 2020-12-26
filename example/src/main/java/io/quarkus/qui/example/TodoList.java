package io.quarkus.qui.example;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.LayoutManager;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import io.quarkus.qui.Props;
import io.quarkus.qui.View;
import io.quarkus.qui.skija.SkijaWindow;
import io.quarkus.qui.ui.ComponentView;
import io.quarkus.qui.ui.Text;
import io.quarkus.qui.ui.VLayout;

public class TodoList implements View<TodoList.TodoListProps> {
    public interface TodoListProps extends Props<TodoListProps> {
        TodoListProps todo(List<String> tasks);
    }

    private Component content;

    @PostConstruct
    void setContent() {
        var panel = new javax.swing.JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, BoxLayout.PAGE_AXIS));
        var y = -25;

        var label = new JLabel("iter 0");
        label.setBounds(10, y += 35, 160, 25);
        panel.add(label);

        var combobox = new javax.swing.JComboBox<>(new String[]{"javax.swing.JComboBox"});
        combobox.setBounds(10, y += 35, 160, 25);
        panel.add(combobox);

        var checkbox = new javax.swing.JCheckBox("Checkbox", false);
        checkbox.setBounds(10, y += 35, 160, 25);
        panel.add(checkbox);

        checkbox = new javax.swing.JCheckBox("Checkbox", true);
        checkbox.setBounds(10, y += 35, 160, 25);
        panel.add(checkbox);

        var radio = new javax.swing.JRadioButton("JRadioButton", false);
        radio.setBounds(10, y += 35, 160, 25);
        panel.add(radio);

        radio = new javax.swing.JRadioButton("JRadioButton", true);
        radio.setBounds(10, y += 35, 160, 25);
        panel.add(radio);

        var slider = new javax.swing.JSlider() {
            @Override public java.awt.Point getMousePosition() throws HeadlessException { return null; }
        };
        slider.setBounds(10, y += 35, 160, 25);
        panel.add(slider);

        var textfield = new javax.swing.JTextField("JTextField");
        textfield.setBounds(10, y += 35, 160, 25);
        panel.add(textfield);

        var textarea = new javax.swing.JTextArea("JTextArea");
        textarea.setBounds(10, y += 35, 160, 25);
        panel.add(textarea);

        var progress = new javax.swing.JProgressBar();
        progress.setValue(30);
        progress.setBounds(10, y += 35, 160, 25);
        panel.add(progress);

        panel.setSize(180, y + 35);
        content = panel;
    }

    @Override
    public Props<?> render(TodoListProps props) {
        var tasks = get(props::todo);
        return show(VLayout.class)
                  /*.children(showItems(Text.class,
                                      tasks,
                                      (task, taskProps) -> taskProps.text(task)
                  ))*/
                  .withChild(show(ComponentView.class).component(content));
    }
}
