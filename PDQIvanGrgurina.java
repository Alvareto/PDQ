/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.fer.tel.rassus.pdq.examples;

import Jama.Matrix;
import com.perfdynamics.pdq.Methods;
import com.perfdynamics.pdq.Node;
import com.perfdynamics.pdq.PDQ;
import com.perfdynamics.pdq.QDiscipline;

/**
 *
 * @author Ivan Grgurina
 */
public class PDQIvanGrgurina {

    /*
    2.3 Zadnja znamenka JMBAG-a 5, 6, 7
    Vjerojatnosti prosljeđivanja paketa u sustavu:
    a = 0.2, b = 0.3, c = 0.5, d = 0.3, e = 0.7, f = 0.5, g = 0.1 i h = 0.3
    Srednja vremena obrade paketa:
    S1 = 0.003 s/p, S2 = 0.001 s/p, S3 = 0.01 s/p, S4 = 0.04 s/p, S5 = 0.1 s/p, S6 = 0.13 s/p i
    S7 = 0.15 s/p
     */
    public static void main(String[] args) {
        PDQ pdq = new PDQ();

        // učestalost dolazaka zahtjeva u rep čekanja
        // # arival rate into the network
        final float lambda = 0.1f; // 0.1 zahtjeva per second

        // # node service times
        float[] stime = {0.003f, 0.001f, 0.01f, 0.04f, 0.1f, 0.13f, 0.15f};
        // srednja vremena obrade paketa // S1 = 1.0 seconds per packet
        // prosječno vrijeme posluživanja zahtjeva

        // vjerojatnosti prosljeđivanja paketa u sustavu
        final float a = 0.2f;
        final float b = 0.3f;
        final float c = 0.5f;
        //
        final float d = 0.3f;
        final float e = 0.7f;
        //
        final float f = 0.5f;
        final float g = 0.1f;
        //
        final float h = 0.3f;

        // # set up traffic eqns from network diagram
        double[][] array = {
            {1f, 0f, -f, 0f},
            {-a - b - c, 1., 0f, 0f},
            {0f, -d, 1f, -h},
            {0f, -e, -g, 1f}
        };
        double[][] result = {{1f}, {0f}, {0f}, {0f}};
        Matrix A = new Matrix(array);
        Matrix B = new Matrix(result);
        // # solve for local throughputs (L)
        double[][] L = A.solve(B).getArray(); // might throw exception
        // # use L matrix to calculate visit ratios at each router
        double[] v = {L[0][0] / lambda, L[1][0] / lambda, L[2][0] / lambda, L[3][0] / lambda};

        // # service demands at each node
        double[] sd = {
            lambda * stime[0], // C1
            v[0] * stime[1], // C2
            v[1] * stime[2], // C3
            v[1] * stime[3], // C4
            v[1] * stime[4], // C5
            v[2] * stime[5], // C6
            v[3] * stime[6] // C7
        };

        // postavljanje početnih postavki PDQ sustava
        pdq.Init("Sustav 7 posluzitelja");

        String wname = "Zahtjevi";
        String[] rname = {"Poslužitelj1", "Poslužitelj2", "Poslužitelj3", "Poslužitelj4", "Poslužitelj5", "Poslužitelj6", "Poslužitelj7"};

        // stwaranje ulaznog toka zahtjeva
        // # create the traffic arriving into the network
        pdq.CreateOpen(wname, lambda);

        // # create network routers
        for (int i = 0; i < rname.length; i++) {
            // stvaranje 7 poslužitelja koji zahtjeve poslužu prema redoslijedu prispjeća
            pdq.CreateNode(rname[i], Node.CEN, QDiscipline.FCFS);
            // povezivanje ulaznog toga s poslužiteljima
            pdq.SetDemand(rname[i], wname, sd[i]);
        }

        // pokretanje izračuna
        pdq.Solve(Methods.CANON);

        // prikaz rezultata
        pdq.Report();
    }
}
