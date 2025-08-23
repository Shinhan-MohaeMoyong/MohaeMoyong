package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.adapter.deposit.DemandDepositApiAdapter;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireDemandDepositAccountListResponse;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireDemandDepositListResponse;
import shinhan.mohaemoyong.server.domain.ProductListResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServive {
    private final DemandDepositApiAdapter adapter;

    @Transactional(readOnly = true)
    public List<ProductListResponse> getProductList() {
        InquireDemandDepositListResponse adapterDto = adapter.inquireDemandDepositList();

        List<InquireDemandDepositListResponse.Record> records = adapterDto.getREC();

        return records.stream()
                .map(ProductListResponse::toDto)
                .collect(Collectors.toList());

    }
}
