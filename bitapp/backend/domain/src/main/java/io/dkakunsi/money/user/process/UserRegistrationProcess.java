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
  public ProcessResult<User> process(ProcessInput<UserRegistrationInput> input) {
    try {
      User user = userPort.findByEmail(input.data().email())
          .map(existing -> update(existing, input.data()))
          .orElseGet(() -> create(input.data()));
      return ProcessResult.success(user);
    } catch (IllegalArgumentException e) {
      return ProcessResult.failure(ProcessError.Code.BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      return ProcessResult.failure(ProcessError.Code.SERVER_ERROR, e.getMessage());
    }
  }

  private User update(User existingUser, UserRegistrationInput userInput) {
    return existingUser.needUpdate(userInput) ? userPort.save(existingUser.update(userInput)) : existingUser;
  }

  private User create(UserRegistrationInput userInput) {
    return userPort.save(User.createNew(userInput));
  }
}
