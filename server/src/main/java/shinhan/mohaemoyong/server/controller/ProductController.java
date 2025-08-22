package shinhan.mohaemoyong.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shinhan.mohaemoyong.server.domain.ProductListResponse;
import shinhan.mohaemoyong.server.dto.ProductServive;
import shinhan.mohaemoyong.server.service.AccountListServive;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductServive productServive;

    @GetMapping("/list")
    public ResponseEntity<List<ProductListResponse>> getProductList () {
        List<ProductListResponse> response = productServive.getProductList();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
