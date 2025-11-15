package io.dkakunsi.money.user.process;

import io.dkakunsi.lab.common.process.Process;
import io.dkakunsi.lab.common.process.ProcessError;
import io.dkakunsi.lab.common.process.ProcessInput;
import io.dkakunsi.lab.common.process.ProcessResult;
import io.dkakunsi.money.user.model.User;
import io.dkakunsi.money.user.port.UserPort;

public final class UserRegistrationProcess implements Process<UserRegistrationInput, User> {

  private UserPort userPort;

  public UserRegistrationProcess(UserPort userPort) {
    this.userPort = userPort;
  }

  @Override
  public ProcessResult<User> process(
      ProcessInput<UserRegistrationInput> input) {
    try {
      var existingUser = userPort.findByEmail(input.data().email());
      User user;
      if (existingUser.isEmpty()) {
        user = create(input.data());
      } else if (existingUser.get().needUpdate(input.data())) {
        user = update(existingUser.get(), input.data());
      } else {
        user = existingUser.get();
      }
      return ProcessResult.success(user);
    } catch (IllegalArgumentException e) {
      return ProcessResult.failure(ProcessError.Code.BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      return ProcessResult.failure(ProcessError.Code.SERVER_ERROR, e.getMessage());
    }
  }

  private User update(User existingUser, UserRegistrationInput userInput) {
    var updatedUser = existingUser.update(userInput);
    return userPort.save(updatedUser);
  }

  private User create(UserRegistrationInput userInput) {
    var newUser = User.createNew(userInput);
    return userPort.save(newUser);
  }
}
