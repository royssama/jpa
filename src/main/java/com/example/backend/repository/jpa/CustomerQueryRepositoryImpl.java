package com.example.backend.repository.jpa;

import com.example.backend.domain.Customer;
import com.example.backend.domain.QCustomer;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomerQueryRepositoryImpl implements CustomerQueryRepository {

    private final JPAQueryFactory queryFactory;

    public CustomerQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<Customer> findByKeywordContainingIgnoreCase(String keyword) {
        QCustomer customer = QCustomer.customer;
        BooleanExpression match = customer.firstName.containsIgnoreCase(keyword)
                .or(customer.lastName.containsIgnoreCase(keyword))
                .or(customer.email.containsIgnoreCase(keyword));
        return queryFactory
                .selectFrom(customer)
                .where(match)
                .fetch();
    }
}
