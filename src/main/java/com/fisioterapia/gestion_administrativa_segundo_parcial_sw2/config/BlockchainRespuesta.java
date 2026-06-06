package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BlockchainRespuesta {
    private String hash;
    private String txHash;
    private String timestamp;
}
