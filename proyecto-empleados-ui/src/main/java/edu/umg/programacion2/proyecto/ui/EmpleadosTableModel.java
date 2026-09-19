package edu.umg.programacion2.proyecto.ui;

import edu.umg.programacion2.proyecto.modelo.Empleado;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class EmpleadosTableModel extends AbstractTableModel {

    private static final String[] COLUMNAS = {
            "ID", "Nombre", "Departamento", "Salario", "Fecha contratación", "Activo",  "Años experiencia"
    };

    private List<Empleado> empleados = new ArrayList<>();

    public void setEmpleados(List<Empleado> empleados) {
        this.empleados = empleados;
        fireTableDataChanged();
    }

    public Empleado getEmpleadoEn(int fila) {
        return empleados.get(fila);
    }

    @Override
    public int getRowCount() { return empleados.size(); }

    @Override
    public int getColumnCount() { return COLUMNAS.length; }

    @Override
    public String getColumnName(int columna) { return COLUMNAS[columna]; }

    @Override
    public Object getValueAt(int fila, int columna) {
        Empleado e = empleados.get(fila);
        switch (columna) {
            case 0: return e.getId();
            case 1: return e.getNombre();
            case 2: return e.getDepartamento();
            case 3: return "Q" + e.getSalario();
            case 4: return e.getFechaContratacion();
            case 5: return e.isActivo() ? "Activo" : "Inactivo";
            case 6: return e.getAniosExperiencia();
            default: return null;
        }
    }
}