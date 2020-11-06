package common;

import exceptions.NotInversibleInZnException;

import java.util.List;

public class MathUtils {

    /**
     * Fonction fournie pour trouver l'inverse de A dans Zn.
     * */
    public static int modInverseOfAInZn(int a, int n) {
        a = customModulo(a, n);
        for (int i = 1; i < n; i++) {
            if (customModulo((a * i),n) == 1) {
                return i;
            }
        }
        throw new NotInversibleInZnException(a + " is not inversible in Z(" + n + ")");
    }


    /**
     * Fonctions qui retourne l'évaluation polynômilae de 'x', selon les coefficients passés dans 'coefficients'.
     * Le calcul assume que les coefficents sont en ordre croissant de puissance de x : a0, a1, a2 ...
     * Dans le genre:
     *
     * a0*x^0 + a1*x^1 + a2*x^2 + ... + ai*x^i
     */
    public static int polynomialEvaluationOf(int x, List<Integer> coefficients) {

        int polyEvalOfx = 0;
        for (int j = 0; j < coefficients.size(); j++) {
            polyEvalOfx += coefficients.get(j) * Math.pow(x, j); //on calcule f(i)
        }

        return polyEvalOfx;
    }

    /**
     * Modulo qui prend en compte les nombres négatifs pour les ramener dans Zn.
     * Ex: -1 mod 26 = 25
     * */
    public static int customModulo(int moduloOf, int moduloIn) {
        int r = moduloOf % moduloIn;
        return r < 0 ? r + moduloIn : r;
    }

    /**
     * Fonction qui vérifie si un nombre est premier
     * */
    public static boolean isPrime(int number) {
        for (int i = 2; i < number; i++) {
            if (number % i == 0)
                return false;
        }

        return true;
    }
}
