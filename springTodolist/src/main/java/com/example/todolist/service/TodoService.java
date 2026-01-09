package com.example.todolist.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoData;
import com.example.todolist.form.TodoQuery;
import com.example.todolist.repository.TodoRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;

    // 入力チェック（登録・更新）
    public boolean isValid(TodoData todoData, BindingResult result) {
        boolean ans = true;

        // 件名チェック（全角スペースのみ）
        String title = todoData.getTitle();
        if (title != null && !title.isEmpty()) {
            boolean isAllDoubleSpace = true;
            for (int i = 0; i < title.length(); i++) {
                if (title.charAt(i) != '　') {
                    isAllDoubleSpace = false;
                    break;
                }
            }
            if (isAllDoubleSpace) {
                result.addError(new FieldError(
                        result.getObjectName(),
                        "title",
                        "件名が全角スペースです"));
                ans = false;
            }
        }

        // 期限チェック
        String deadline = todoData.getDeadline();
        if (deadline != null && !deadline.isEmpty()) {
            try {
                LocalDate deadlineDate = LocalDate.parse(deadline);
                if (deadlineDate.isBefore(LocalDate.now())) {
                    result.addError(new FieldError(
                            result.getObjectName(),
                            "deadline",
                            "期限は今日以降にしてください"));
                    ans = false;
                }
            } catch (DateTimeException e) {
                result.addError(new FieldError(
                        result.getObjectName(),
                        "deadline",
                        "yyyy-MM-dd形式で入力してください"));
                ans = false;
            }
        }
        return ans;
    }

    // 検索条件チェック
    public boolean isValid(TodoQuery todoQuery, BindingResult result) {
        boolean ans = true;

        if (!todoQuery.getDeadlineFrom().isEmpty()) {
            try {
                LocalDate.parse(todoQuery.getDeadlineFrom());
            } catch (DateTimeException e) {
                result.addError(new FieldError(
                        result.getObjectName(),
                        "deadlineFrom",
                        "yyyy-MM-dd形式で入力してください"));
                ans = false;
            }
        }

        if (!todoQuery.getDeadlineTo().isEmpty()) {
            try {
                LocalDate.parse(todoQuery.getDeadlineTo());
            } catch (DateTimeException e) {
                result.addError(new FieldError(
                        result.getObjectName(),
                        "deadlineTo",
                        "yyyy-MM-dd形式で入力してください"));
                ans = false;
            }
        }
        return ans;
    }

    // 検索処理
    public List<Todo> doQuery(TodoQuery todoQuery) {

        if (todoQuery.getTitle() != null && !todoQuery.getTitle().isEmpty()) {
            return todoRepository.findByTitleLike("%" + todoQuery.getTitle() + "%");

        } else if (todoQuery.getImportance() != null && todoQuery.getImportance() != -1) {
            return todoRepository.findByImportance(todoQuery.getImportance());

        } else if (todoQuery.getUrgency() != null && todoQuery.getUrgency() != -1) {
            return todoRepository.findByUrgency(todoQuery.getUrgency());

        } else if (!todoQuery.getDeadlineFrom().isEmpty() && todoQuery.getDeadlineTo().isEmpty()) {
            return todoRepository
                    .findByDeadlineGreaterThanEqualOrderByDeadlineAsc(
                            Utils.str2date(todoQuery.getDeadlineFrom()));

        } else if (todoQuery.getDeadlineFrom().isEmpty() && !todoQuery.getDeadlineTo().isEmpty()) {
            return todoRepository
                    .findByDeadlineLessThanEqualOrderByDeadlineAsc(
                            Utils.str2date(todoQuery.getDeadlineTo()));

        } else if (!todoQuery.getDeadlineFrom().isEmpty() && !todoQuery.getDeadlineTo().isEmpty()) {
            return todoRepository
                    .findByDeadlineBetweenOrderByDeadlineAsc(
                            Utils.str2date(todoQuery.getDeadlineFrom()),
                            Utils.str2date(todoQuery.getDeadlineTo()));

        } else if ("Y".equals(todoQuery.getDone())) {
            return todoRepository.findByDone("Y");
        }

        return todoRepository.findAll();
    }
}
