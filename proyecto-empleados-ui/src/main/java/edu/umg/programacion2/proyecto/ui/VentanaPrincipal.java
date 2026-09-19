package edu.umg.programacion2.proyecto.ui;

import edu.umg.programacion2.proyecto.dao.EmpleadoDAO;
import edu.umg.programacion2.proyecto.modelo.Empleado;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
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
    private static final Color COLOR_FILA_PAR = new Color(245, 247, 250);
    private static final Color COLOR_FILA_IMPAR = Color.WHITE;
    private static final Color COLOR_ELIMINAR = new Color(214, 69, 65);

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
        setSize(900, 560);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 12));

        JPanel panelRaiz = new JPanel(new BorderLayout(0, 12));
        panelRaiz.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(panelRaiz);

        panelRaiz.add(construirTitulo(), BorderLayout.NORTH);
        panelRaiz.add(construirPanelTabla(), BorderLayout.CENTER);
        panelRaiz.add(construirPanelFormulario(), BorderLayout.SOUTH);

        btnNuevo.addActionListener(this::onNuevo);
        btnGuardar.addActionListener(this::onGuardar);
        btnEliminar.addActionListener(this::onEliminar);
        btnRefrescar.addActionListener(e -> cargarEmpleados());
    }

    private JLabel construirTitulo() {
        JLabel titulo = new JLabel("Gestión de Empleados");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        titulo.setBorder(new EmptyBorder(0, 4, 8, 0));
        return titulo;
    }

    private JScrollPane construirPanelTabla() {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(28);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setFont(tabla.getFont().deriveFont(14f));
        tabla.getTableHeader().setFont(tabla.getFont().deriveFont(Font.BOLD, 14f));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 34));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? COLOR_FILA_PAR : COLOR_FILA_IMPAR);
                }
                return c;
            }
        };
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        tabla.getColumnModel().getColumn(0).setCellRenderer(renderer); // ID
        tabla.getColumnModel().getColumn(5).setCellRenderer(renderer); // Activo

        DefaultTableCellRenderer rendererIzquierda = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? COLOR_FILA_PAR : COLOR_FILA_IMPAR);
                }
                return c;
            }
        };
        tabla.getColumnModel().getColumn(1).setCellRenderer(rendererIzquierda); // Nombre
        tabla.getColumnModel().getColumn(2).setCellRenderer(rendererIzquierda); // Departamento
        tabla.getColumnModel().getColumn(3).setCellRenderer(rendererIzquierda); // Salario
        tabla.getColumnModel().getColumn(4).setCellRenderer(rendererIzquierda); // Fecha

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 223, 228)));
        return scroll;
    }

    private JPanel construirPanelFormulario() {
        JPanel panelCampos = new JPanel(new GridBagLayout());
        panelCampos.setBorder(BorderFactory.createTitledBorder("Datos del empleado"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        agregarCampo(panelCampos, gbc, 0, 0, "Nombre completo:", txtNombre);
        agregarCampo(panelCampos, gbc, 2, 0, "Departamento:", txtDepartamento);
        agregarCampo(panelCampos, gbc, 0, 1, "Salario mensual:", txtSalario);
        agregarCampo(panelCampos, gbc, 2, 1, "Fecha contratación (yyyy-MM-dd):", txtFecha);

        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        panelCampos.add(chkActivo, gbc);

        for (Component c : panelCampos.getComponents()) {
            c.setFont(c.getFont().deriveFont(13f));
        }

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setBackground(COLOR_ELIMINAR);
        btnEliminar.setOpaque(true);
        btnEliminar.setBorderPainted(false);
        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnRefrescar);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.add(panelCampos, BorderLayout.CENTER);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);
        return panelInferior;
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int col, int fila, String etiqueta, JComponent campo) {
        gbc.gridx = col;
        gbc.gridy = fila;
        gbc.weightx = 0;
        JLabel label = new JLabel(etiqueta);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        panel.add(label, gbc);

        gbc.gridx = col + 1;
        gbc.weightx = 1;
        panel.add(campo, gbc);
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
