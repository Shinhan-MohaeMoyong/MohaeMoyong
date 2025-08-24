package shinhan.mohaemoyong.server.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import shinhan.mohaemoyong.server.domain.PlanPhotos;

import java.util.List;

public interface PlanPhotoRepository extends JpaRepository<PlanPhotos, Long> {
    List<PlanPhotos> findByPlan_PlanIdOrderByOrderNoAscPlanPhotoIdAsc(Long planId);

    // 치환 시 “남아있지 않은 기존 사진 일괄 삭제”에 사용
    void deleteByPlan_PlanIdAndPlanPhotoIdIn(Long planId, List<Long> ids);
}