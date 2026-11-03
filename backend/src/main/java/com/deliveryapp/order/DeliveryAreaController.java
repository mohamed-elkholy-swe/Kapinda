package com.deliveryapp.order;

import java.util.List;
import com.deliveryapp.account.AccountRole;
import com.deliveryapp.account.AccountService;
import com.deliveryapp.account.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class DeliveryAreaController implements org.springframework.boot.CommandLineRunner {
  private final DeliveryAreaRepository areas;
  private final AccountService accounts;

  public DeliveryAreaController(DeliveryAreaRepository areas, AccountService accounts) {
    this.areas = areas;
    this.accounts = accounts;
  }
  public record Area(String name, int fee) {}

  private static final List<Area> AREAS = List.of(
    new Area("ش١٤", 10),
    new Area("الميدان", 10),
    new Area("سور النادي", 10),
    new Area("التقسيم", 10),
    new Area("المهندسين", 10),
    new Area("سوق الثلاثاء", 10),
    new Area("عزبه مسلم", 15),
    new Area("الابوة", 10),
    new Area("صدفه", 10),
    new Area("عزبه احمد فوزي", 10),
    new Area("ارض بني", 10),
    new Area("ارض أبو جمعة", 10),
    new Area("الباسوسيه", 10),
    new Area("ألجزيره", 30),
    new Area("الشرفة", 30),
    new Area("كفر عليم", 40),
    new Area("المنيره", 40),
    new Area("منصور عامر", 40),
    new Area("عزبه آياتي", 20),
    new Area("الحادثة", 30),
    new Area("ابو الغيط", 40),
    new Area("باسوس", 70),
    new Area("قليوب", 60),
    new Area("شلقان", 30),
    new Area("بهادة", 30),
    new Area("البرادعه", 40),
    new Area("سندبيس", 50),
    new Area("اجهور", 50),
    new Area("الأهالي", 30),
    new Area("ضروه", 50)
  );

  @Override
  @Transactional
  public void run(String... args) {
    for (int index = 0; index < AREAS.size(); index++) {
      Area area = AREAS.get(index);
      areas.seed(index + 1, area.name(), area.fee());
    }
  }

  @GetMapping("/api/delivery-areas")
  public List<DeliveryArea> list() {
    return areas.findAllByOrderByIdAsc();
  }

  public record UpdateArea(@NotBlank @Size(max = 80) String name, @NotNull @PositiveOrZero @Max(999999) Integer fee) {}

  @PutMapping("/api/admin/delivery-areas/{id}")
  @Transactional
  public DeliveryArea update(@RequestAttribute AuthenticatedUser authenticatedUser, @PathVariable int id,
                             @Valid @RequestBody UpdateArea request) {
    accounts.requireRole(authenticatedUser, AccountRole.ADMIN);
    var area = areas.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    String name = request.name().trim();
    if (areas.existsByNameAndIdNot(name, id)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "A delivery area already has this name");
    }
    area.update(name, request.fee());
    return area;
  }
}
