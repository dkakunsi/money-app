package io.dkakunsi.money.user.process;

import io.dkakunsi.common.process.Process;
import io.dkakunsi.common.process.ProcessError;
import io.dkakunsi.common.process.ProcessInput;
import io.dkakunsi.common.process.ProcessResult;
import io.dkakunsi.money.user.model.User;
import io.dkakunsi.money.user.port.UserPort;

public final class RegisterUserProcess implements Process<RegisterUserInput, User> {

  private UserPort userPort;

  public RegisterUserProcess(UserPort userPort) {
    this.userPort = userPort;
  }

  @Override
  public ProcessResult<User> process(
      ProcessInput<RegisterUserInput> input) {
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
    } catch (Exception e) {
      return ProcessResult.failure(ProcessError.Code.SERVER_ERROR, e.getMessage());
    }
  }

  private User update(User existingUser, RegisterUserInput userInput) {
    var updatedUser = existingUser.update(userInput);
    return userPort.save(updatedUser);
  }

  private User create(RegisterUserInput userInput) {
    var newUser = User.createNew(userInput);
    return userPort.save(newUser);
  }
}
