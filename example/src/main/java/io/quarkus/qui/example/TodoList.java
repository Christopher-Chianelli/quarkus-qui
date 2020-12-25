package io.quarkus.qui.example;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.annotation.PostConstruct;
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
        content = new JTextField();
        // content.setForeground(Color.WHITE);
        // content.setBackground(Color.BLACK);
        content.setSize(100, 100);
    }

    @Override
    public Props<?> render(TodoListProps props) {
        var tasks = get(props::todo);
        return show(VLayout.class)
                  .children(showItems(Text.class,
                                      tasks,
                                      (task, taskProps) -> taskProps.text(task)
                  ))
                  .withChild(show(ComponentView.class).component(content));
    }
}
