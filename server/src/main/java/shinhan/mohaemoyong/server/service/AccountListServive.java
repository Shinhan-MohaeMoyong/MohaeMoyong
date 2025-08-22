package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.adapter.deposit.DemandDepositApiAdapter;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireDemandDepositAccountListResponse;
import shinhan.mohaemoyong.server.dto.SimpleAccountListResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountListServive {

    private final DemandDepositApiAdapter adapter;

    @Transactional(readOnly = true)
    public List<SimpleAccountListResponse> getSimpleAccountList(String userkey) {
        InquireDemandDepositAccountListResponse adapterDto = adapter.inquireDemandDepositAccountList(userkey);

        List<InquireDemandDepositAccountListResponse.Record> records = adapterDto.getREC();

        // Null 또는 빈 리스트일 경우, NullPointerException을 방지하기 위해 빈 리스트를 반환
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        return records.stream()
                .map(SimpleAccountListResponse::toDto)
                .collect(Collectors.toList());
    }

}
