package io.dkakunsi.money.user.process;

import io.dkakunsi.lab.common.process.Process;
import io.dkakunsi.lab.common.process.ProcessError;
import io.dkakunsi.lab.common.process.ProcessInput;
import io.dkakunsi.lab.common.process.ProcessResult;
import io.dkakunsi.money.user.model.User;
import io.dkakunsi.money.user.port.UserPort;

public final class UserRetrievalProcess implements Process<UserRetrievalInput, User> {

  private UserPort userPort;

  public UserRetrievalProcess(UserPort userPort) {
    this.userPort = userPort;
  }

  @Override
  public ProcessResult<User> process(ProcessInput<UserRetrievalInput> input) {
    try {
      return userPort.findByEmail(input.data().email())
          .map(user -> ProcessResult.success(user))
          .orElse(ProcessResult.success());
    } catch (Exception e) {
      return ProcessResult.failure(ProcessError.Code.SERVER_ERROR, e.getMessage());
    }
  }
}
