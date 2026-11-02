package com.deliveryapp.notification;
import com.deliveryapp.account.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
@RestController @RequestMapping("/api/notifications/devices")
public class PushController {
 public record Device(@NotBlank @Size(max=4096) String token) {}
 private final PushService service;
 public PushController(PushService service){this.service=service;}
 @PutMapping @ResponseStatus(HttpStatus.NO_CONTENT)
 public void register(@RequestAttribute AuthenticatedUser authenticatedUser,@Valid @RequestBody Device device){service.register(authenticatedUser,device.token());}
 @DeleteMapping @ResponseStatus(HttpStatus.NO_CONTENT)
 public void unregister(@RequestAttribute AuthenticatedUser authenticatedUser,@Valid @RequestBody Device device){service.unregister(authenticatedUser,device.token());}
}
