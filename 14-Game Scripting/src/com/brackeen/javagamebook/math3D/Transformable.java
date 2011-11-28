package com.brackeen.javagamebook.math3D;

public interface Transformable {

    public void add(Vector3D u);

    public void subtract(Vector3D u);

    public void add(Transform3D xform);

    public void subtract(Transform3D xform);

    public void addRotation(Transform3D xform);

    public void subtractRotation(Transform3D xform);

}
