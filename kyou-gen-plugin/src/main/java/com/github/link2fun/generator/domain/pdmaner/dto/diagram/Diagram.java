package com.github.link2fun.generator.domain.pdmaner.dto.diagram;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class Diagram implements Serializable {
  private String defKey;
  private String defName;
  private DiagramCanvasData canvasData;

}
