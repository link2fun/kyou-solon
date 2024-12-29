package com.github.link2fun.generator.domain.pdmaner.dto.diagram;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagramCanvasDataCell implements Serializable {

  private String id;
  private String shape;
  private DiagramCanvasDataCellPosition position;
  private String count;
  private String originKey;

}
