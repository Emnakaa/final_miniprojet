package com.planning.controller;

import com.planning.dao.DBConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@WebServlet(name = "AuthController", urlPatterns = {"/auth"})
public class AuthController extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        
        String action = req.getParameter("action");
        
        if ("login".equals(action)) {
            String username = req.getParameter("username");
            String password = req.getParameter("password");
            
            try {
                int userId = authenticateUser(username, password);
                if (userId > 0) {
                    out.print("{\"status\": \"ok\", \"userId\": " + userId + "}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.print("{\"status\": \"error\", \"message\": \"Invalid credentials\"}");
                }
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\": \"error\", \"message\": \"Database error\"}");
                e.printStackTrace();
            }
        } else if ("register".equals(action)) {
            String nom = req.getParameter("nom");
            String prenom = req.getParameter("prenom");
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            
            if (nom == null || nom.isEmpty() || email == null || email.isEmpty() || password == null || password.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\": \"error\", \"message\": \"Missing fields\"}");
                return;
            }
            
            try {
                if (userExists(email)) {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print("{\"status\": \"error\", \"message\": \"Email already exists\"}");
                } else {
                    int userId = registerUser(nom, prenom, email, password);
                    if (userId > 0) {
                        out.print("{\"status\": \"ok\", \"userId\": " + userId + "}");
                    } else {
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.print("{\"status\": \"error\", \"message\": \"Registration failed\"}");
                    }
                }
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\": \"error\", \"message\": \"Database error\"}");
                e.printStackTrace();
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"status\": \"error\", \"message\": \"Invalid action\"}");
        }
    }
    
    private int authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT id FROM utilisateurs WHERE email = ? AND mot_de_passe = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }
    
    private boolean userExists(String email) throws SQLException {
        String sql = "SELECT id FROM utilisateurs WHERE email = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    private int registerUser(String nom, String prenom, String email, String password) throws SQLException {
        String sql = "INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role) VALUES (?, ?, ?, ?, 'USER')";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setString(3, email);
            ps.setString(4, password);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }
}
