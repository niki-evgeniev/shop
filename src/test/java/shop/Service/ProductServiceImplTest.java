package shop.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import shop.DTO.Porduct.AddProductDTO;
import shop.DTO.Porduct.DetailsProductDTO;
import shop.DTO.Porduct.ListAllProductDTO;
import shop.Entity.Picture;
import shop.Entity.Product;
import shop.Repository.PictureRepository;
import shop.Repository.ProductRepository;
import shop.Service.Impl.ProductServiceImpl;
import shop.Util.ConvertorBgToEn;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PictureRepository pictureRepository;

    @Mock
    private ConvertorBgToEn convertorBgToEn;

    @Mock
    private MultipartFile multipartFile;

    private ModelMapper modelMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;

    @BeforeEach
    void setUp() {
        modelMapper = new ModelMapper();
        productService = new ProductServiceImpl(productRepository, pictureRepository, convertorBgToEn, modelMapper);

        product = new Product();
        product.setId(1L);
        product.setName("Лаптоп");
        product.setModel("Acer");
        product.setPriceBgn(BigDecimal.valueOf(1200));
        product.setUrl("laptop-acer");
    }

    @Test
    void testCountProducts() {
        when(productRepository.count()).thenReturn(5L);

        long count = productService.countProducts();

        assertThat(count).isEqualTo(5);
        verify(productRepository, times(1)).count();
    }

    @Test
    void testListAllProduct() {
        Page<Product> mockPage = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        Picture picture = new Picture();
        picture.setProduct(product);
        picture.setImagePath("image1.jpg");
        when(pictureRepository.findAll()).thenReturn(List.of(picture));

        Page<ListAllProductDTO> result = productService.listAllProduct(Pageable.unpaged());

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Лаптоп");
        assertThat(result.getContent().get(0).getImageFile1()).isEqualTo("image1.jpg");
    }

    @Test
    void testGetDetails_ProductFound() {
        when(productRepository.findByUrl("laptop-acer")).thenReturn(Optional.of(product));

        Picture pic1 = new Picture();
        pic1.setImagePath("pic1.jpg");
        pic1.setProduct(product);

        Picture pic2 = new Picture();
        pic2.setImagePath("pic2.jpg");
        pic2.setProduct(product);

        when(pictureRepository.findAllByProductId(1L)).thenReturn(List.of(pic1, pic2));

        DetailsProductDTO result = productService.getDetails("laptop-acer");

        assertThat(result).isNotNull();
        assertThat(result.getImageFile1()).isEqualTo("pic1.jpg");
        assertThat(result.getImageFile2()).isEqualTo("pic2.jpg");
    }

    @Test
    void testGetDetails_ProductNotFound() {
        when(productRepository.findByUrl("unknown")).thenReturn(Optional.empty());

        DetailsProductDTO result = productService.getDetails("unknown");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isNull();
    }

    @Test
    void testAddProduct_Success() {
        AddProductDTO dto = new AddProductDTO();
        dto.setName("Лаптоп");
        dto.setModel("Acer");
        dto.setDescription("Гейминг лаптоп");
        dto.setPrice(BigDecimal.valueOf(1200));

        when(convertorBgToEn.convertCyrillicToLatin(anyString())).thenReturn("laptop");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        dto.setImageFile1(file);
        dto.setImageFile2(file);
        dto.setImageFile3(file);
        dto.setImageFile4(file);

        productService.addProduct(dto);

        verify(convertorBgToEn, atLeastOnce()).convertCyrillicToLatin(anyString());
    }
}
