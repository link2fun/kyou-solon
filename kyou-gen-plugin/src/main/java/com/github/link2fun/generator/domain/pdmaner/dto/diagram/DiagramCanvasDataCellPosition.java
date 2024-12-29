package com.github.link2fun.generator.domain.pdmaner.dto.diagram;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagramCanvasDataCellPosition implements Serializable {
  private Integer x;
  private Integer y;

}
