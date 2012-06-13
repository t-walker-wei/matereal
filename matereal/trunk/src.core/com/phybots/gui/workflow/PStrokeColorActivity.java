/*
 * PROJECT: Phybots at http://phybots.com/
 * ----------------------------------------------------------------------------
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Phybots.
 *
 * The Initial Developer of the Original Code is Jun Kato.
 * Portions created by the Initial Developer are
 * Copyright (C) 2009 Jun Kato. All Rights Reserved.
 *
 * Contributor(s): Jun Kato
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */
package com.phybots.gui.workflow;

import java.awt.Color;

import edu.umd.cs.piccolo.activities.PInterpolatingActivity;

/**
 * <b>PStrokeColorActivity</b> interpolates between two stroke colors for its
 * target over the duration of the animation. The source color is retrieved from
 * the target just before the activity is scheduled to start.
 */
public class PStrokeColorActivity extends PInterpolatingActivity {

    private Color source;
    private Color destination;
    private final Target target;

    /**
     * <b>Target</b> Objects that want their color to be set by the color
     * activity must implement this interface.
     */
    public interface Target {

        /**
         * This will be called by the color activity for each new interpolated
         * color that it computes while it is stepping.
         *
         * @param color the color to assign to the target
         */
        void setStrokeColor(Color color);

        /**
         * This method is called right before the color activity starts. That
         * way an object's color is always animated from its current color.
         *
         * @return the target's current color.
         */
        Color getStrokeColor();
    }

    /**
     * Constructs a color activity for the given target that will animate for
     * the duration provided at an interval of stepRate.
     *
     * Destination color must be assigned later.
     *
     * @param duration duration in milliseconds that the animation should last
     * @param stepRate the time between interpolations
     * @param aTarget the target onto which the animation is being performed
     */
    public PStrokeColorActivity(final long duration, final long stepRate, final Target aTarget) {
        this(duration, stepRate, aTarget, null);
    }

    /**
     * Constructs a color activity for the given target that will animate for
     * the duration provided at an interval of stepRate from the target's
     * starting color to the destination color.
     *
     * @param duration duration in milliseconds that the animation should last
     * @param stepRate the time between interpolations
     * @param aTarget the target onto which the animation is being performed
     * @param aDestination the color to which the animation is aiming at
     */
    public PStrokeColorActivity(final long duration, final long stepRate, final Target aTarget, final Color aDestination) {
        this(duration, stepRate, 1, PInterpolatingActivity.SOURCE_TO_DESTINATION, aTarget, aDestination);
    }

    /**
     * Create a new PColorActivity.
     *
     * @param duration the length of one loop of the activity
     * @param stepRate the amount of time between steps of the activity
     * @param loopCount number of times the activity should reschedule itself
     * @param mode defines how the activity interpolates between states
     * @param aTarget the object that the activity will be applied to and where
     *            the source state will be taken from.
     * @param aDestination the destination color state
     */
    public PStrokeColorActivity(final long duration, final long stepRate, final int loopCount, final int mode,
            final Target aTarget, final Color aDestination) {
        super(duration, stepRate, loopCount, mode);
        target = aTarget;
        destination = aDestination;
    }

    /**
     * Returns true since all PColorActivities animate the scene.
     *
     * @return always returns true
     */
    protected boolean isAnimation() {
        return true;
    }

    /**
     * Return the final color that will be set on the color activities target
     * when the activity stops stepping.
     *
     * @return the final color for this color activity
     */
    public Color getDestinationColor() {
        return destination;
    }

    /**
     * Set the final color that will be set on the color activities target when
     * the activity stops stepping.
     *
     * @param newDestination to animate towards
     */
    public void setDestinationColor(final Color newDestination) {
        destination = newDestination;
    }

    /**
     * Overrides it's parent to ensure that the source color is the color of the
     * node being animated.
     */
    protected void activityStarted() {
        if (getFirstLoop()) {
            source = target.getStrokeColor();
        }
        super.activityStarted();
    }

    /**
     * Interpolates the target node's color by mixing the source color and the
     * destination color.
     *
     * @param zeroToOne 0 = all source color, 1 = all destination color
     */
    public void setRelativeTargetValue(final float zeroToOne) {
        super.setRelativeTargetValue(zeroToOne);
        final float red = source.getRed() + zeroToOne * (destination.getRed() - source.getRed());
        final float green = source.getGreen() + zeroToOne * (destination.getGreen() - source.getGreen());
        final float blue = source.getBlue() + zeroToOne * (destination.getBlue() - source.getBlue());
        final float alpha = source.getAlpha() + zeroToOne * (destination.getAlpha() - source.getAlpha());
        target.setStrokeColor(new Color((int) red, (int) green, (int) blue, (int) alpha));
    }
}