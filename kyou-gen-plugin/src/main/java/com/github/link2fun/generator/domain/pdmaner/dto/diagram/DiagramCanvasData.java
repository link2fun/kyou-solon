package com.github.link2fun.generator.domain.pdmaner.dto.diagram;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class DiagramCanvasData implements Serializable {

  private List<DiagramCanvasDataCell> cells;
}
