package shinhan.mohaemoyong.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinhan.mohaemoyong.server.adapter.deposit.DemandDepositApiAdapter;
import shinhan.mohaemoyong.server.adapter.deposit.dto.response.InquireDemandDepositAccountListResponse;
import shinhan.mohaemoyong.server.domain.Accounts;
import shinhan.mohaemoyong.server.dto.SimpleAccountListResponse;
import shinhan.mohaemoyong.server.repository.AccountRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountListServive {

    private final DemandDepositApiAdapter adapter;
    private final AccountRepository accountRepository;


    @Transactional(readOnly = true)
    public List<SimpleAccountListResponse> getSimpleAccountList(String userkey) {
        InquireDemandDepositAccountListResponse adapterDto = adapter.inquireDemandDepositAccountList(userkey);

        List<InquireDemandDepositAccountListResponse.Record> records = adapterDto.getREC();

        // Null 또는 빈 리스트일 경우, NullPointerException을 방지하기 위해 빈 리스트를 반환
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 외부에서 받아온 정보에서 계좌번호 리스트를 추출합니다.
        List<String> accountNumbers = records.stream()
                .map(InquireDemandDepositAccountListResponse.Record::getAccountNo)
                .collect(Collectors.toList());

        // 3. 추출한 계좌번호들을 사용해 우리 DB에서 해당 계좌 정보를 '한 번에' 조회합니다. (N+1 문제 방지)
        List<Accounts> ourAccounts = accountRepository.findAllByAccountNumberIn(accountNumbers);

        // 4. 빠른 조회를 위해 조회된 DB 데이터를 '계좌번호'를 Key로 하는 Map으로 변환합니다.
        Map<String, Accounts> ourAccountMap = ourAccounts.stream()
                .collect(Collectors.toMap(Accounts::getAccountNumber, account -> account));

        // 5. stream의 map 안에서 수정된 toDto 메서드를 호출하여 데이터 조합
        return records.stream()
                .map(record -> {
                    // Map에서 현재 record에 해당하는 우리 DB 정보를 찾습니다. (없으면 null)
                    Accounts ourAccount = ourAccountMap.get(record.getAccountNo());
                    // 두 객체를 모두 toDto 메서드로 전달합니다.
                    return SimpleAccountListResponse.toDto(record, ourAccount);
                })
                .collect(Collectors.toList());
    }

}
