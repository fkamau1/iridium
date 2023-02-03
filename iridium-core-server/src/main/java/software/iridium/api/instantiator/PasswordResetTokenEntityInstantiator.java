/*
 *  Copyright (C) Josh Fischer - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Josh Fischer <josh@joshfischer.io>, 2023.
 */
package software.iridium.api.instantiator;

import java.util.Date;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import software.iridium.api.entity.IdentityEntity;
import software.iridium.api.entity.PasswordResetTokenEntity;
import software.iridium.api.util.DateUtils;

@Component
public class PasswordResetTokenEntityInstantiator {

  @Value("${passwordRestToken.lifetimeHours}")
  private Integer passwordResetTokenLifetime;

  @Resource private BCryptPasswordEncoder encoder;

  @Transactional(propagation = Propagation.REQUIRED)
  public PasswordResetTokenEntity instantiate(final IdentityEntity identity) {
    final var resetToken = new PasswordResetTokenEntity();
    String token = encoder.encode(new Date() + identity.getId());
    resetToken.setIdentity(identity);
    resetToken.setToken(token);
    identity.setPasswordResetToken(resetToken);
    resetToken.setExpiration(DateUtils.addHoursToCurrentTime(passwordResetTokenLifetime));
    return resetToken;
  }
}