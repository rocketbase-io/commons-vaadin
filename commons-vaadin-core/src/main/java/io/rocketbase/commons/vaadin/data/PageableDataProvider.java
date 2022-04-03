package io.rocketbase.commons.vaadin.data;

import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class PageableDataProvider {

    public interface FetchPagableApi<T,F> {
        Page<T> fetchFromBackEnd(Query<T, F> query, Pageable pageable);

        default List<QuerySortOrder> getDefaultSortOrders() {
            return new ArrayList<>();
        }
    }

    public interface CountPagableApi<T,F> {
        Page<T> sizeInBackEnd(Query<T, F> query, Pageable pageable);
    }

    public static <T, F> CallbackDataProvider.FetchCallback<T, F> fetch(FetchPagableApi<T, F> api) {
        return query -> api.fetchFromBackEnd(query, PageRequest.of(query.getPage(), query.getPageSize(), createSpringSort(query, api.getDefaultSortOrders()))).stream();
    }

    public static <T, F> CallbackDataProvider.CountCallback<T, F> count(CountPagableApi<T,F> api) {
        return query -> (int) api.sizeInBackEnd(query, PageRequest.of(0, 1)).getTotalElements();
    }


    protected static Sort.Order queryOrderToSpringOrder(QuerySortOrder queryOrder) {
        return new Sort.Order(queryOrder.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC, queryOrder.getSorted());
    }


    protected static <T, F> Sort createSpringSort(Query<T, F> query, List<QuerySortOrder> defaultSort) {
        List<QuerySortOrder> sortOrders;
        if (query.getSortOrders().isEmpty()) {
            sortOrders = defaultSort;
        } else {
            sortOrders = query.getSortOrders();
        }
        List<Sort.Order> orders = sortOrders.stream().map(PageableDataProvider::queryOrderToSpringOrder).collect(Collectors.toList());
        if (orders.isEmpty()) {
            return Sort.unsorted();
        } else {
            return Sort.by(orders);
        }
    }


}
