package com.example.demo.app.task;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Task;
import com.example.demo.service.TaskService;

/**
 * ToDoアプリ
 */
@Controller
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }


    /**
     * タスクの一覧を表示します
     * @param taskForm
     * @param model
     * @return resources/templates下のHTMLファイル名
     */
    @GetMapping
    public String task(TaskForm taskForm, Model model) {
    	//新規登録か更新かを判断する仕掛け
        taskForm.setNewTask(true);
        //Taskのリストを取得する
        List<Task> list = taskService.findAll();
        model.addAttribute("list", list);
        model.addAttribute("title", "タスク一覧");

        return "task/index";
    }

    /**
     * タスクデータを一件挿入
     * @param taskForm
     * @param result
     * @param model
     * @return
     */
    @PostMapping("/insert")
    public String insert(
    	@Valid @ModelAttribute TaskForm taskForm,
        BindingResult result,
        Model model) {

        if (!result.hasErrors()) {
        	//TaskFormのデータをTaskに格納
            Task task = new Task();
            task = makeTask(taskForm ,0);
            taskService.insert(task);
        	//一件挿入後リダイレクト
            return "redirect:/task";
        } else {
            taskForm.setNewTask(true);
            model.addAttribute("taskForm", taskForm);
            List<Task> list = taskService.findAll();
            model.addAttribute("list", list);
            model.addAttribute("title", "タスク一覧（バリデーション）");
            return "task/index";
        }
    }

    /**
     * 一件タスクデータを取得し、フォーム内に表示
     * @param taskForm
     * @param id
     * @param model
     * @return
     */
    @GetMapping("/{id}")
    public String showUpdate(
    	TaskForm taskForm,
        @PathVariable int id,
        Model model) {

    	//Taskを取得(Optionalでラップ)
        Optional<Task> taskOpt = taskService.getTask(id);
        //TaskFormへの詰め直し
        Optional<TaskForm> taskFormOpt = taskOpt.map(task -> makeTaskForm(task));
        //TaskFormがnullでなければ中身を取り出し
        if (taskFormOpt.isPresent()) {
            taskForm = taskFormOpt.get();
        }

        model.addAttribute("taskForm", taskForm);
        List<Task> list = taskService.findAll();
        model.addAttribute("list", list);
        model.addAttribute("taskId", id);
        model.addAttribute("title", "更新用フォーム");

        return "task/index";
    }

    /**
     * タスクidを取得し、一件のデータ更新
     * @param taskForm
     * @param result
     * @param model
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/update")
    public String update(
    	@Valid @ModelAttribute TaskForm taskForm,
    	BindingResult result,
    	@RequestParam("taskId") int taskId,
    	Model model,
    	RedirectAttributes redirectAttributes) {

        if (!result.hasErrors()) {
        	//TaskFormのデータをTaskに格納
            Task task = makeTask(taskForm, taskId);
        	//更新処理、フラッシュスコープの使用、リダイレクト（個々の編集ページ）
            taskService.update(task);
            redirectAttributes.addFlashAttribute("complete", "変更が成功しました。");
            return "redirect:/task/" + taskId;
        } else {
            model.addAttribute("taskForm", taskForm);
            model.addAttribute("title", "タスク一覧");
            return "task/index";
        }
    }

    /**
     * タスクidを取得し、一件のデータ削除
     * @param id
     * @param model
     * @return
     */
    @PostMapping("/delete")
    public String delete(
    	@RequestParam("taskId") int id,
    	Model model) {
    	//タスクを一件削除しリダイレクト
        taskService.deleteById(id);
        return "redirect:/task";
    }

    /**
     * 複製用に一件タスクデータを取得し、フォーム内に表示
     * @param taskForm
     * @param id
     * @param model
     * @return
     */
    //1-1　"/duplicate"に対してマッピングを行うアノテーションを記述する
    public String duplicate(
    	TaskForm taskForm,
    	//1-2　Requestパラメータから"taskId"の名前でint idを取得するようにする
    	int id,
        Model model) {

    	//1-3　taskService.getTaskを用いてTaskを取得する
        Optional<Task> taskOpt = null;

        //TaskFormへの詰め直し
        Optional<TaskForm> taskFormOpt = taskOpt.map(t -> makeTaskForm(t));

        //TaskFormがnullでなければ中身を取り出し
        if(taskFormOpt.isPresent()) {
        	taskForm = taskFormOpt.get();
        }

        //新規登録のためNewTaskにtrueをセット
        taskForm.setNewTask(true);

        model.addAttribute("taskForm", taskForm);
        List<Task> list = taskService.findAll();
        model.addAttribute("list", list);
        model.addAttribute("title", "タスク一覧");

        return "task/index";
    }

    /**
     * 選択したタスクタイプのタスク一覧を表示
     * @param taskForm
     * @param id
     * @param model
     * @return
     */
    //2-4 "/selectType"に対してマッピングを行うアノテーションを記述する
    public String selectType(
    	TaskForm taskForm,
    	//2-5 Requestパラメータから"typeId"の名前でint idを取得するようにする
    	int id,
        Model model) {

    	//新規登録か更新かを判断する仕掛け
        taskForm.setNewTask(true);

        //2-6 taskService.findByTypeを用いてTaskのリストを取得する
        List<Task> list = null;

        model.addAttribute("list", list);
        model.addAttribute("title", "タスク一覧");

        return "task/index";
    }


    /**
     * TaskFormのデータをTaskに入れて返す
     * @param taskForm
     * @param taskId 新規登録の場合は0を指定
     * @return
     */
    private Task makeTask(TaskForm taskForm, int taskId) {
        Task task = new Task();
        if(taskId != 0) {
        	task.setId(taskId);
        }
        task.setUserId(1);
        task.setTypeId(taskForm.getTypeId());
        task.setTitle(taskForm.getTitle());
        task.setDetail(taskForm.getDetail());
        task.setDeadline(taskForm.getDeadline());
        return task;
    }

    /**
     * TaskのデータをTaskFormに入れて返す
     * @param task
     * @return
     */
    private TaskForm makeTaskForm(Task task) {

        TaskForm taskForm = new TaskForm();

        taskForm.setTypeId(task.getTypeId());
        taskForm.setTitle(task.getTitle());
        taskForm.setDetail(task.getDetail());
        taskForm.setDeadline(task.getDeadline());
        taskForm.setNewTask(false);

        return taskForm;
    }
}
