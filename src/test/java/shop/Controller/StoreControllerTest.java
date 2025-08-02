package shop.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import shop.DTO.Porduct.DetailsProductDTO;
import shop.DTO.Porduct.ListAllProductDTO;
import shop.Service.ProductService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = shop.ShopApplication.class)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@AutoConfigureMockMvc
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private ListAllProductDTO productDTO;
    private DetailsProductDTO detailsProductDTO;

    @BeforeEach
    void setUp() {
        productDTO = new ListAllProductDTO();
        productDTO.setId(1L);
        productDTO.setName("Test Product");
        productDTO.setModel("Model X");
        productDTO.setPriceBgn(BigDecimal.valueOf(100.0));
        productDTO.setImageFile1("image.jpg");

        detailsProductDTO = new DetailsProductDTO();
        detailsProductDTO.setName("Test Product");
        detailsProductDTO.setModel("Model X");
        detailsProductDTO.setPriceBgn(BigDecimal.valueOf(100.0));
        detailsProductDTO.setImageFile1("image.jpg");
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testStorePageLoadsProducts() throws Exception {
        Page<ListAllProductDTO> mockPage = new PageImpl<>(List.of(productDTO));
        when(productService.listAllProduct(any(Pageable.class))).thenReturn(mockPage);
        when(productService.countProducts()).thenReturn(1L);

        mockMvc.perform(get("/store"))
                .andExpect(status().isOk())
                .andExpect(view().name("storeList"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("countProducts", 1L));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testDetailsPageProductFound() throws Exception {
        when(productService.getDetails("test-product")).thenReturn(detailsProductDTO);

        mockMvc.perform(get("/store/details/test-product"))
                .andExpect(status().isOk())
                .andExpect(view().name("storeProductDetails"))
                .andExpect(model().attributeExists("detailsProductDTO"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void testDetailsPageProductNotFound() throws Exception {
        DetailsProductDTO notFoundDTO = new DetailsProductDTO();
        notFoundDTO.setName(null);
        notFoundDTO.setModel(null);

        when(productService.getDetails("unknown")).thenReturn(notFoundDTO);

        mockMvc.perform(get("/store/details/unknown"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/pageNotFound"))
                .andExpect(model().attribute("productNotFound", true));
    }
}
