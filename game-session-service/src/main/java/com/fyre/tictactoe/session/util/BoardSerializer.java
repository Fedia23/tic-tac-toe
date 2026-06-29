package com.fyre.tictactoe.session.util;

import com.fyre.tictactoe.session.config.BoardConfig;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
public class BoardSerializer {

    private static final String DELIMITER = ",";
    private final BoardConfig boardConfig;

    public String serialize(List<String> board) {
        return String.join(DELIMITER, board);
    }

    public List<String> deserialize(String serializedBoard) {
        if (serializedBoard == null || serializedBoard.isEmpty()) {
            return Collections.nCopies(boardConfig.getSize(), "");
        }
        return List.of(serializedBoard.split(DELIMITER));
    }
}