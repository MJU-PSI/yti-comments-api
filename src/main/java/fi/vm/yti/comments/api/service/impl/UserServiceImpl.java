package fi.vm.yti.comments.api.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import fi.vm.yti.comments.api.configuration.GroupManagementProperties;
import fi.vm.yti.comments.api.dto.UserDTO;
import fi.vm.yti.comments.api.service.UserService;
import static fi.vm.yti.comments.api.constants.ApiConstants.GROUPMANAGEMENT_API_CONTEXT_PATH;
import static fi.vm.yti.comments.api.constants.ApiConstants.GROUPMANAGEMENT_API_USERS;
import static org.springframework.http.HttpMethod.GET;

@Component
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private final Map<UUID, UserDTO> users;
    private final GroupManagementProperties groupManagementProperties;
    private final RestTemplate restTemplate;

    @Inject
    public UserServiceImpl(final GroupManagementProperties groupManagementProperties,
                           final RestTemplate restTemplate) {
        this.groupManagementProperties = groupManagementProperties;
        this.restTemplate = restTemplate;
        users = new HashMap<>();
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void fetchUsers() {
        updateUsers();
    }

    public void updateUsers() {
        String url = groupManagementProperties.getUrl() + "/" + GROUPMANAGEMENT_API_CONTEXT_PATH + "/" + GROUPMANAGEMENT_API_USERS;
        final Set<UserDTO> fetchedUsers = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<Set<UserDTO>>() {
        }).getBody();
        if (fetchedUsers != null) {
            LOG.info(String.format("Successfully synced %d users from groupmanagement service!", fetchedUsers.size()));
        }
        Objects.requireNonNull(fetchedUsers).forEach(user -> users.put(user.getId(), user));
    }

    public UserDTO getUserById(final UUID id) {
        return users.get(id);
    }

    public String getUserEmailById(final UUID id) {
        return users.get(id).getEmail();
    }
}
