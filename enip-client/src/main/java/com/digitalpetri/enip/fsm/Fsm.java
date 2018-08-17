package com.digitalpetri.enip.fsm;

public interface Fsm<E> {

    void fireEvent(E event);

    interface State<F extends Fsm<E>, S extends Fsm.State<F, S, E>, E> {

        /**
         * Evaluate {@code event} in the context of the current state, execute necessary
         * actions or logic, and then return the state to transition to.
         *
         * @param fsm   the {@link Fsm}.
         * @param event the triggering event.
         * @return the state to transition to.
         */
        S evaluate(F fsm, E event);

        /**
         * Called after an event triggers an internal transition to this state.
         *
         * @param fsm   the session {@link Fsm}.
         * @param event the triggering event.
         */
        default void onInternalTransition(F fsm, E event) {}

        /**
         * Called after an event triggers an external transition to this state.
         *
         * @param fsm   the session {@link Fsm}.
         * @param prev  the previous state..
         * @param event the triggering event.
         */
        default void onExternalTransition(F fsm, S prev, E event) {}

    }

}
