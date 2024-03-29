package br.com.italomiranda.todolist.tasks;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ITaskRepository extends JpaRepository<TaskModel, UUID> {
    //Busca no DB apartir do usuario do parametro todas as suas tasks
    List<TaskModel> findByIdUser(UUID idUser);

}
