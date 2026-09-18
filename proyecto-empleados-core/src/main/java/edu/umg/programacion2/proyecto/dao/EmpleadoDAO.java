package edu.umg.programacion2.proyecto.dao;

import edu.umg.programacion2.proyecto.modelo.Empleado;
import edu.umg.programacion2.proyecto.util.ConexionBD;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmpleadoDAO {

    private static final String SQL_INSERT ="INSERT INTO empleados (nombre, departamento, salario, fecha_contratacion, activo) " + "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_ALL ="SELECT id, nombre, departamento, salario, fecha_contratacion, activo " + "FROM empleados ORDER BY id";

    private static final String SQL_SELECT_BY_ID ="SELECT id, nombre, departamento, salario, fecha_contratacion, activo " + "FROM empleados WHERE id = ?";

    private static final String SQL_UPDATE ="UPDATE empleados SET nombre = ?, departamento = ?, salario = ?, " + "fecha_contratacion = ?, activo = ? WHERE id = ?";

    private static final String SQL_DELETE ="DELETE FROM empleados WHERE id = ?";

    public Empleado crear(Empleado item) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, item.getNombre());
            ps.setString(2, item.getDepartamento());
            ps.setBigDecimal(3, item.getSalario());
            ps.setDate(4, Date.valueOf(item.getFechaContratacion()));
            ps.setBoolean(5, item.isActivo());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setId(keys.getInt(1));
                }
            }
        }
        return item;
    }

    public List<Empleado> listarTodos() throws SQLException {
        List<Empleado> resultado = new ArrayList<>();

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resultado.add(mapearFila(rs));
            }
        }
        return resultado;
    }

    public Optional<Empleado> buscarPorId(int id) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_SELECT_BY_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearFila(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean actualizar(Empleado item) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, item.getNombre());
            ps.setString(2, item.getDepartamento());
            ps.setBigDecimal(3, item.getSalario());
            ps.setDate(4, Date.valueOf(item.getFechaContratacion()));
            ps.setBoolean(5, item.isActivo());
            ps.setInt(6, item.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Empleado mapearFila(ResultSet rs) throws SQLException {
        return new Empleado(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("departamento"),
                rs.getBigDecimal("salario"),
                rs.getDate("fecha_contratacion").toLocalDate(),
                rs.getBoolean("activo")
        );
    }
}