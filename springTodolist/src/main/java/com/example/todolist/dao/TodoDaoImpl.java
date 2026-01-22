package com.example.todolist.dao;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.todolist.common.Utils;
import com.example.todolist.entity.Todo;
import com.example.todolist.entity.Todo_; // ← ★重要
import com.example.todolist.form.TodoQuery;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TodoDaoImpl implements TodoDao {

	private final EntityManager entityManager;

	// Criteria APIによる検索
	@Override
	public Page<Todo> findByCriteria(TodoQuery todoQuery, Pageable pageable) {

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Todo> query = builder.createQuery(Todo.class);
		Root<Todo> root = query.from(Todo.class);

		List<Predicate> predicates = new ArrayList<>();

		// 件名
		if (todoQuery.getTitle() != null && !todoQuery.getTitle().isEmpty()) {
			predicates.add(
					builder.like(
							root.get(Todo_.TITLE),
							"%" + todoQuery.getTitle() + "%"));
		}

		// 重要度
		if (todoQuery.getImportance() != -1) {
			predicates.add(
					builder.equal(
							root.get(Todo_.IMPORTANCE),
							todoQuery.getImportance()));
		}

		// 緊急度
		if (todoQuery.getUrgency() != -1) {
			predicates.add(
					builder.equal(
							root.get(Todo_.URGENCY),
							todoQuery.getUrgency()));
		}

		// 期限：開始～
		if (todoQuery.getDeadlineFrom() != null && !todoQuery.getDeadlineFrom().isEmpty()) {
			predicates.add(
					builder.greaterThanOrEqualTo(
							root.get(Todo_.DEADLINE),
							Utils.str2date(todoQuery.getDeadlineFrom())));
		}

		// ～期限：終了
		if (todoQuery.getDeadlineTo() != null && !todoQuery.getDeadlineTo().isEmpty()) {
			predicates.add(
					builder.lessThanOrEqualTo(
							root.get(Todo_.DEADLINE),
							Utils.str2date(todoQuery.getDeadlineTo())));
		}

		// 完了
		if ("Y".equals(todoQuery.getDone())) {
			predicates.add(
					builder.equal(
							root.get(Todo_.DONE),
							todoQuery.getDone()));
		}

//		// WHERE句・ORDER BY
//		query.select(root)
//				.where(predicates.toArray(new Predicate[0]))
//				.orderBy(builder.asc(root.get(Todo_.ID)));
//
//		// 検索実行
//		return (Page<Todo>) entityManager.createQuery(query).getResultList();
		
		// SELECT作成 　　
		Predicate[] predArray = new Predicate[ predicates.size()]; 
		predicates.toArray( predArray); 
		query = query.select( root).where( predArray).orderBy( builder.asc( root.get(Todo_. id)));
		// クエリ生成 　　
		TypedQuery<Todo> typedQuery = entityManager.createQuery( query);//　① 　　
		// 該当レコード数取得 　　 
		int totalRows = typedQuery.getResultList().size();//　②
		// 先頭レコードの位置設定 　　 
		typedQuery.setFirstResult( pageable.getPageNumber() * pageable.getPageSize());//　③ 　　
		// 1ページ当たりの件数 　　 
		typedQuery.setMaxResults( pageable.getPageSize());//　④
		
		Page<Todo> page = new PageImpl<Todo>( typedQuery.getResultList(), pageable, totalRows); //⑤ 　　 
		return page;//　⑥
		
		
		
		
		
		
		
	}
}
