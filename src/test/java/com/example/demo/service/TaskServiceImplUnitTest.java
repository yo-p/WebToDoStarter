package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dao.TaskDao;
import com.example.demo.entity.Task;
import org.springframework.dao.EmptyResultDataAccessException;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskServiceImplの単体テスト")
class TaskServiceImplUnitTest {

    @Mock // モック(stub)クラス ダミーオブジェクト
    private TaskDao dao;

    @InjectMocks // テスト対象クラス　モックを探す newする
    private TaskServiceImpl taskServiceImpl;

    @Test // テストケース
    @DisplayName("テーブルtaskの全件取得で0件の場合のテスト")
        // テスト名
    void testFindAllReturnEmptyList() {

    	//空のリスト
    	List<Task> list = new ArrayList<>();

        // モッククラスのI/Oをセット（findAll()の型と異なる戻り値はNG）
        when(dao.findAll()).thenReturn(list);

        // サービスを実行
        List<Task> actualList= taskServiceImpl.findAll();

        // モックの指定メソッドの実行回数を検査
        verify(dao, times(1)).findAll();

        // 戻り値の検査(expected, actual)
        assertEquals(0, actualList.size());

    }

    @Test // テストケース
    @DisplayName("テーブルTaskの全件取得で2件の場合のテスト")
        // テスト名
    void testFindAllReturnList() {

    	//モックから返すListに2つのTaskオブジェクトをセット
    	List<Task> list = new ArrayList<>();
    	Task task1 = new Task();
    	Task task2 = new Task();
    	list.add(task1);
    	list.add(task2);

        // モッククラスのI/Oをセット（findAll()の型と異なる戻り値はNG）
        when(dao.findAll()).thenReturn(list);

        // サービスを実行
        List<Task> actualList= taskServiceImpl.findAll();

        // モックの指定メソッドの実行回数を検査
        verify(dao, times(1)).findAll();

        // 戻り値の検査(expected, actual)
        assertEquals(2, actualList.size());

    }

    @Test // テストケース
    @DisplayName("タスクが取得できない場合のテスト")
        // テスト名
    void testGetTaskThrowException() {
        // モッククラスのI/Oをセット
        when(dao.findById(0)).thenThrow(new EmptyResultDataAccessException(1));
        //タスクが取得できないとTaskNotFoundExceptionが発生することを検査
        try {
            Optional<Task> task = taskServiceImpl.getTask(0);
        } catch (TaskNotFoundException e) {
            assertEquals(e.getMessage(), "指定されたタスクは存在しません。");
        }
    }

    @Test // テストケース
    @DisplayName("タスクを1件取得した場合のテスト")
        // テスト名
    void testGetTaskReturnOne() {
    	//Taskをデフォルト値でインスタンス化
        Task task = new Task();
        task.setId(1);
        task.setUserId(1);
        task.setTypeId(1);
        task.setTitle("JUnit");
        task.setDeadline(LocalDateTime.now());
        Optional<Task> taskOpt = Optional.of(task);
        // モッククラスのI/Oをセット
        when(dao.findById(1)).thenReturn(taskOpt);
        // サービスを実行
        Optional<Task> actual = taskServiceImpl.getTask(1);
        // モックの指定メソッドの実行回数を検査
        verify(dao, times(1)).findById(1);
        //Taskが存在していることを確認
        assertTrue(actual.isPresent());
    }

    @Test // テストケース　ユニットテストではデータベースの例外は考えない
    @DisplayName("削除対象が存在しない場合、例外が発生することを確認するテスト")
        // テスト名
    void throwNotFoundException() {
        // モッククラスのI/Oをセット
        when(dao.deleteById(0)).thenReturn(0);
    	//削除対象が存在しない場合、例外が発生することを検査
        try {
            taskServiceImpl.deleteById(0);
        } catch (TaskNotFoundException e) {
            assertEquals(e.getMessage(), "削除するタスクが存在しません。");
        }

    }
}
