package shop.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.DTO.EmailRequestDTO;
import shop.Service.Impl.MailjetService;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MailjetServiceTest {

    private MailjetService mailjetService;

    @BeforeEach
    void setup() throws Exception {
        mailjetService = new MailjetService();

        // Чрез рефлексия сетваме private полетата
        setPrivateField(mailjetService, "apiKey", "testKey");
        setPrivateField(mailjetService, "secretKey", "testSecret");
        setPrivateField(mailjetService, "fromEmail", "from@test.com");
        setPrivateField(mailjetService, "fromName", "Test Sender");
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testSendEmailRegisterUser_FailureReturnsFalse() {
        // Подготвяме DTO
        EmailRequestDTO dto = new EmailRequestDTO();
        dto.setToEmail("user@example.com");
        dto.setToName("User");
        dto.setSubject("Test Subject");
        dto.setTextContent("Test text");
        dto.setHtmlContent("<b>Test HTML</b>");

        // Понеже нямаме реален API, очакваме false (няма връзка)
        boolean result = mailjetService.sendEmailRegisterUser(dto);

        assertThat(result).isFalse();
    }
}
