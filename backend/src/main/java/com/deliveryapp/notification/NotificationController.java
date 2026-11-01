package com.deliveryapp.notification;
import com.deliveryapp.account.AuthenticatedUser;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/notifications")
public class NotificationController {
 private final NotificationService service;
 public NotificationController(NotificationService service){this.service=service;}
 @PutMapping("/read-all") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
 public void readAll(@RequestAttribute AuthenticatedUser authenticatedUser){service.readAll(authenticatedUser);}
 @DeleteMapping @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
 public void removeAll(@RequestAttribute AuthenticatedUser authenticatedUser){service.removeAll(authenticatedUser);}
 @GetMapping public List<NotificationService.Item> list(@RequestAttribute AuthenticatedUser authenticatedUser){return service.list(authenticatedUser);}
 @PutMapping("/{id}/read") @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void read(@RequestAttribute AuthenticatedUser authenticatedUser,@PathVariable String id){service.read(authenticatedUser,id);}
}
