package io.quarkus.qui.example;

import java.util.List;

import io.quarkus.qui.Props;
import io.quarkus.qui.View;
import io.quarkus.qui.ui.Text;
import io.quarkus.qui.ui.VLayout;

public class TodoList implements View<TodoList.TodoListProps> {
    public interface TodoListProps extends Props<TodoListProps> {
        TodoListProps todo(List<String> tasks);
    }

    @Override
    public Props<?> render(TodoListProps props) {
        var tasks = get(props::todo);
        return show(VLayout.class)
                  .children(showItems(Text.class,
                                      tasks,
                                      (task, taskProps) -> taskProps.text(task)
                  ));
    }
}
