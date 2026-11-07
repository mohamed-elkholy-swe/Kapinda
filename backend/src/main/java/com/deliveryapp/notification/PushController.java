package com.deliveryapp.notification;
import com.deliveryapp.account.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/notifications/device")
public class PushController {
  private final PushService service;
  public PushController(PushService service){this.service=service;}
  public record Device(@NotBlank @Size(max=2048) String token) {}
  @PutMapping @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void register(@RequestAttribute AuthenticatedUser authenticatedUser,@Valid @RequestBody Device body){service.register(authenticatedUser,body.token());}
  @DeleteMapping @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void unregister(@RequestAttribute AuthenticatedUser authenticatedUser,@Valid @RequestBody Device body){service.unregister(authenticatedUser,body.token());}
}
