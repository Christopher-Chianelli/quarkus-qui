package io.quarkus.qui.example;

import java.util.List;
import java.util.stream.Collectors;

import io.quarkus.qui.Drawable;
import io.quarkus.qui.Props;
import io.quarkus.qui.View;
import io.quarkus.qui.ui.VLayout;

public class TodoList implements View<TodoList.TodoListProps> {
    public interface TodoListProps extends Props<TodoListProps> {
        TodoListProps todo(List<String> tasks);
    }

    @Override
    public Props<?> render(TodoListProps props) {
        var tasks = get(props::todo);
        return show(VLayout.class)
                  .children(tasks.stream().map(task -> (Drawable) (canvas) -> {
                      System.out.println("Task " + task);
                  }).collect(Collectors.toList()));
    }
}
