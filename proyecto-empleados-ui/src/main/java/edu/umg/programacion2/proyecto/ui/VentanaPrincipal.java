package edu.umg.programacion2.proyecto.ui;

import edu.umg.programacion2.proyecto.dao.EmpleadoDAO;
import edu.umg.programacion2.proyecto.modelo.Empleado;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ISO_LOCAL_DATE;

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final EmpleadosTableModel tableModel = new EmpleadosTableModel();
    private final JTable tabla = new JTable(tableModel);

    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtDepartamento = new JTextField(15);
    private final JTextField txtSalario = new JTextField(10);
    private final JTextField txtFecha = new JTextField(10);
    private final JCheckBox chkActivo = new JCheckBox("Activo", true);

    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnRefrescar = new JButton("Refrescar");

    private Empleado empleadoSeleccionado = null;

    public VentanaPrincipal() {
        super("Gestión de Empleados");
        construirInterfaz();
        cargarEmpleados();
    }

    private void construirInterfaz() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(construirPanelFormulario(), BorderLayout.SOUTH);

        btnNuevo.addActionListener(this::onNuevo);
        btnGuardar.addActionListener(this::onGuardar);
        btnEliminar.addActionListener(this::onEliminar);
        btnRefrescar.addActionListener(e -> cargarEmpleados());
    }

    private JPanel construirPanelFormulario() {
        JPanel panelCampos = new JPanel(new GridLayout(2, 4, 8, 8));
        panelCampos.setBorder(BorderFactory.createTitledBorder("Datos del empleado"));

        panelCampos.add(new JLabel("Nombre completo:"));
        panelCampos.add(txtNombre);
        panelCampos.add(new JLabel("Departamento:"));
        panelCampos.add(txtDepartamento);

        panelCampos.add(new JLabel("Salario mensual:"));
        panelCampos.add(txtSalario);
        panelCampos.add(new JLabel("Fecha contratación (yyyy-MM-dd):"));
        panelCampos.add(txtFecha);

        JPanel panelActivo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelActivo.add(chkActivo);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnRefrescar);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(panelCampos, BorderLayout.CENTER);
        panelInferior.add(panelActivo, BorderLayout.WEST);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);
        return panelInferior;
    }

    private void cargarEmpleados() {
        try {
            List<Empleado> empleados = empleadoDAO.listarTodos();
            tableModel.setEmpleados(empleados);
        } catch (SQLException ex) {
            mostrarError("No se pudo cargar la lista de empleados.", ex);
        }
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        empleadoSeleccionado = tableModel.getEmpleadoEn(fila);
        txtNombre.setText(empleadoSeleccionado.getNombre());
        txtDepartamento.setText(empleadoSeleccionado.getDepartamento());
        txtSalario.setText(empleadoSeleccionado.getSalario().toPlainString());
        txtFecha.setText(empleadoSeleccionado.getFechaContratacion().format(FORMATO_FECHA));
        chkActivo.setSelected(empleadoSeleccionado.isActivo());
    }

    private void onNuevo(ActionEvent e) {
        empleadoSeleccionado = null;
        tabla.clearSelection();
        limpiarFormulario();
    }

    private void onGuardar(ActionEvent e) {
        Empleado datos;
        try {
            datos = leerYValidarFormulario();
        } catch (ValidacionException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (empleadoSeleccionado == null) {
                empleadoDAO.crear(datos);
                JOptionPane.showMessageDialog(this, "Empleado registrado.");
            } else {
                datos.setId(empleadoSeleccionado.getId());
                boolean actualizado = empleadoDAO.actualizar(datos);
                if (actualizado) {
                    JOptionPane.showMessageDialog(this, "Empleado actualizado.");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No se encontró el empleado a actualizar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            }
            cargarEmpleados();
            onNuevo(null);
        } catch (SQLException ex) {
            mostrarError("No se pudo guardar el empleado.", ex);
        }
    }

    private void onEliminar(ActionEvent e) {
        if (empleadoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona primero un empleado de la tabla.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar permanentemente a \"" + empleadoSeleccionado.getNombre() + "\"?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) return;

        try {
            boolean eliminado = empleadoDAO.eliminar(empleadoSeleccionado.getId());
            if (eliminado) {
                JOptionPane.showMessageDialog(this, "Empleado eliminado.");
            } else {
                JOptionPane.showMessageDialog(this, "El empleado ya no existía.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
            cargarEmpleados();
            onNuevo(null);
        } catch (SQLException ex) {
            mostrarError("No se pudo eliminar el empleado.", ex);
        }
    }

    private Empleado leerYValidarFormulario() throws ValidacionException {
        String nombre = txtNombre.getText().trim();
        String departamento = txtDepartamento.getText().trim();
        String salarioTexto = txtSalario.getText().trim();
        String fechaTexto = txtFecha.getText().trim();

        if (nombre.isEmpty()) throw new ValidacionException("El nombre no puede quedar vacío.");
        if (departamento.isEmpty()) throw new ValidacionException("El departamento no puede quedar vacío.");

        BigDecimal salario;
        try {
            salario = new BigDecimal(salarioTexto);
        } catch (NumberFormatException ex) {
            throw new ValidacionException("El salario debe ser un número (ej. 8500.00).");
        }
        if (salario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionException("El salario debe ser mayor a cero.");
        }

        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaTexto, FORMATO_FECHA);
        } catch (DateTimeParseException ex) {
            throw new ValidacionException("La fecha debe tener el formato yyyy-MM-dd (ej. 2024-03-15).");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new ValidacionException("La fecha de contratación no puede ser una fecha futura.");
        }

        return new Empleado(nombre, departamento, salario, fecha, chkActivo.isSelected());
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtDepartamento.setText("");
        txtSalario.setText("");
        txtFecha.setText("");
        chkActivo.setSelected(true);
    }

    private void mostrarError(String mensajeAmigable, SQLException ex) {
        System.err.println("Error SQL: " + ex.getMessage());
        JOptionPane.showMessageDialog(this, mensajeAmigable + "\nDetalle: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static class ValidacionException extends Exception {
        ValidacionException(String mensaje) {
            super(mensaje);
        }
    }
}
