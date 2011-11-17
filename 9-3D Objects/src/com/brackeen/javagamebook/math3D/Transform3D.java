package com.brackeen.javagamebook.math3D;

/**
    The Transform3D class represents a rotation and translation.
*/
public class Transform3D {

    protected Vector3D location;
    private float cosAngleX;
    private float sinAngleX;
    private float cosAngleY;
    private float sinAngleY;
    private float cosAngleZ;
    private float sinAngleZ;

    /**
        Creates a new Transform3D with no translation or rotation.
    */
    public Transform3D() {
        this(0,0,0);
    }


    /**
        Creates a new Transform3D with the specified translation
        and no rotation.
    */
    public Transform3D(float x, float y, float z) {
        location = new Vector3D(x, y, z);
        setAngle(0,0,0);
    }


    /**
        Creates a new Transform3D
    */
    public Transform3D(Transform3D v) {
        location = new Vector3D();
        setTo(v);
    }


    public Object clone() {
        return new Transform3D(this);
    }


    /**
        Sets this Transform3D to the specified Transform3D.
    */
    public void setTo(Transform3D v) {
        location.setTo(v.location);
        this.cosAngleX = v.cosAngleX;
        this.sinAngleX = v.sinAngleX;
        this.cosAngleY = v.cosAngleY;
        this.sinAngleY = v.sinAngleY;
        this.cosAngleZ = v.cosAngleZ;
        this.sinAngleZ = v.sinAngleZ;
    }


    /**
        Gets the location (translation) of this transform.
    */
    public Vector3D getLocation() {
        return location;
    }

    public float getCosAngleX() {
        return cosAngleX;
    }

    public float getSinAngleX() {
        return sinAngleX;
    }

    public float getCosAngleY() {
        return cosAngleY;
    }

    public float getSinAngleY() {
        return sinAngleY;
    }

    public float getCosAngleZ() {
        return cosAngleZ;
    }

    public float getSinAngleZ() {
        return sinAngleZ;
    }

    public float getAngleX() {
        return (float)Math.atan2(sinAngleX, cosAngleX);
    }

    public float getAngleY() {
        return (float)Math.atan2(sinAngleY, cosAngleY);
    }

    public float getAngleZ() {
        return (float)Math.atan2(sinAngleZ, cosAngleZ);
    }

    public void setAngleX(float angleX) {
        cosAngleX = (float)Math.cos(angleX);
        sinAngleX = (float)Math.sin(angleX);
    }

    public void setAngleY(float angleY) {
        cosAngleY = (float)Math.cos(angleY);
        sinAngleY = (float)Math.sin(angleY);
    }

    public void setAngleZ(float angleZ) {
        cosAngleZ = (float)Math.cos(angleZ);
        sinAngleZ = (float)Math.sin(angleZ);
    }

    public void setAngle(float angleX, float angleY, float angleZ)
    {
        setAngleX(angleX);
        setAngleY(angleY);
        setAngleZ(angleZ);
    }

    public void rotateAngleX(float angle) {
        if (angle != 0) {
            setAngleX(getAngleX() + angle);
        }
    }

    public void rotateAngleY(float angle) {
        if (angle != 0) {
            setAngleY(getAngleY() + angle);
        }
    }

    public void rotateAngleZ(float angle) {
        if (angle != 0) {
            setAngleZ(getAngleZ() + angle);
        }
    }

    public void rotateAngle(float angleX, float angleY,
        float angleZ)
    {
        rotateAngleX(angleX);
        rotateAngleY(angleY);
        rotateAngleZ(angleZ);
    }

}

