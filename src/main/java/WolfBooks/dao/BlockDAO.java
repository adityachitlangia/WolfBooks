package src.main.java.WolfBooks.dao;
import src.main.java.WolfBooks.models.BlockModel;
import src.main.java.WolfBooks.models.StudentActivityModel;
import src.main.java.WolfBooks.util.DatabaseConnection;

import java.awt.image.ByteLookupTable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BlockDAO {

    public boolean addBlock(BlockModel block) {
        String sqlQuery = "INSERT INTO blocks (section_id, textbook_id, block_id, chapter_id, content_type, content, is_hidden, created_by, sequence_number) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            stmt.setString(1, block.getSectionId());
            stmt.setString(2, block.getTextbookId());
            stmt.setString(3, block.getBlockId());
            stmt.setString(4, block.getChapterId());
            stmt.setString(5, block.getContentType());
            stmt.setString(6, block.getContent());
            stmt.setBoolean(7, block.isHidden());
            stmt.setString(8, block.getCreatedBy());
            stmt.setInt(9, block.getSequenceNumber());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean modifyBlock(BlockModel block) {
        String sqlQuery = "UPDATE blocks SET content_type = ?, content = ?, is_hidden = ? " +
                "WHERE section_id = ? AND textbook_id = ? AND block_id = ? AND chapter_id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            stmt.setString(1, block.getContentType());
            stmt.setString(2, block.getContent());
            stmt.setBoolean(3, block.isHidden());
            stmt.setString(4, block.getSectionId());
            stmt.setString(5, block.getTextbookId());
            stmt.setString(6, block.getBlockId());
            stmt.setString(7, block.getChapterId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean deleteBlock(BlockModel block) {
        String sqlQuery = "DELETE FROM blocks WHERE section_id = ? AND textbook_id = ? AND block_id = ? AND chapter_id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            stmt.setString(1, block.getSectionId());
            stmt.setString(2, block.getTextbookId());
            stmt.setString(3, block.getBlockId());
            stmt.setString(4, block.getChapterId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // Find block by section

    public BlockModel findBlock(String textbookId, String chapterId, String sectionId, String blockId) {
        String sqlQuery = "SELECT * FROM blocks WHERE textbook_id = ? AND chapter_id = ? AND section_id = ? AND block_id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            stmt.setString(1, textbookId);
            stmt.setString(2, chapterId);
            stmt.setString(3, sectionId);
            stmt.setString(4, blockId);

            ResultSet rs = stmt.executeQuery();
            return mapResultSetToBlock(rs);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public List<BlockModel> findBlocksBySection(String textbookId, String chapterId, String sectionId) {
                String sqlQuery = "SELECT * FROM blocks WHERE textbook_id = ? AND chapter_id = ? AND section_id = ?";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sqlQuery);
            stmt.setString(1, textbookId);
            stmt.setString(2, chapterId);
            stmt.setString(3, sectionId);

            ResultSet rs = stmt.executeQuery();
            List<BlockModel> blockModels = new ArrayList<>();
            while (rs.next()) {
                blockModels.add(mapResultSetToBlock(rs));
            }

            stmt.close();
            rs.close();
            return blockModels;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private BlockModel mapResultSetToBlock(ResultSet rs) throws SQLException {
        return new BlockModel(
                rs.getString("section_id"),
                rs.getString("textbook_id"),
                rs.getString("block_id"),
                rs.getString("chapter_id"),
                rs.getString("content_type"),
                rs.getString("content"),
                rs.getBoolean("is_hidden"),
                rs.getString("created_by"),
                rs.getInt("sequence_number")
        );
    }
}