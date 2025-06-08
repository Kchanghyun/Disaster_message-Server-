package com.gwnu.fcm_server.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DisasterMessageDTO {
    private String MSG_CN;         // 메시지 내용
    private String RCPTN_RGN_NM;   // 수신 지역
    private String CRT_DT;         // 생성 일시
    private String EMRG_STEP_NM;   // 긴급 단계
    private String DST_SE_NM;      // 재해 구분
    private String SN;             // 일련번호
    private String REG_YMD;        // 등록 일자
    private String MDFCN_YMD;      // 수정 일자
}
