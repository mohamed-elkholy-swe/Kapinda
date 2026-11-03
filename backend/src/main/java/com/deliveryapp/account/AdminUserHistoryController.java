package com.deliveryapp.account;

import com.deliveryapp.order.OrderItemEntity;
import com.deliveryapp.order.OrderRepository;
import com.deliveryapp.order.OrderStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AdminUserHistoryController {
  private static final int PAGE_SIZE = 20;
  private static final ZoneId HISTORY_ZONE = ZoneId.of("Africa/Cairo");
  public record OrderSummary(String id, OrderStatus status, Instant createdAt, List<String> restaurants) {}

  public record HistoryPage(List<OrderSummary> orders, boolean hasNext) {}

  private final AccountService accounts;
  private final AccountRepository users;
  private final OrderRepository orders;

  public AdminUserHistoryController(AccountService accounts, AccountRepository users, OrderRepository orders) {
    this.accounts = accounts;
    this.users = users;
    this.orders = orders;
  }

  @GetMapping("/api/admin/users/{uid}/history")
  @Transactional(readOnly = true)
  public HistoryPage history(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable String uid,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
    @RequestParam(defaultValue = "0") int page) {
    accounts.requireRole(authenticatedUser, AccountRole.ADMIN);
    var user = users.findById(uid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    var restaurantIds = user.getRestaurantIds();
    LocalDate defaultDay = date == null ? LocalDate.now(HISTORY_ZONE) : date;
    LocalDate firstDay = startDate == null ? defaultDay : startDate;
    LocalDate lastDay = endDate == null ? defaultDay : endDate;
    if (lastDay.isBefore(firstDay)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be on or after the start date");
    }
    Instant start = firstDay.atStartOfDay(HISTORY_ZONE).toInstant();
    Instant end = lastDay.plusDays(1).atStartOfDay(HISTORY_ZONE).toInstant();
    if (page < 0 || page > Integer.MAX_VALUE / PAGE_SIZE) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid history page");
    }
    var paging = PageRequest.of(page, PAGE_SIZE);
    if (user.getRole() == AccountRole.RESTAURANT && restaurantIds.isEmpty()) {
      return new HistoryPage(List.of(), false);
    }
    var recent = switch (user.getRole()) {
      case DELIVERY -> orders.findByDeliveryPersonUidAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(uid, start, end, paging);
      case RESTAURANT -> orders.findRecentRestaurantOrders(restaurantIds, start, end, paging);
      default -> orders.findByCustomerUidAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDescIdDesc(uid, start, end, paging);
    };
    return new HistoryPage(recent.stream().map(order -> new OrderSummary(order.getId().toString(), order.getStatus(), order.getCreatedAt(),
      order.getItems().stream()
        .filter(item -> user.getRole() != AccountRole.RESTAURANT || restaurantIds.contains(item.getRestaurantId()))
        .map(OrderItemEntity::getRestaurantName).distinct().toList()
    )).toList(), recent.hasNext());
  }
}
