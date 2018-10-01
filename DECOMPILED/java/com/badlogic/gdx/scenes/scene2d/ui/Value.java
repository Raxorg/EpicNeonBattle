package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

public abstract class Value {
    public static Value maxHeight = new Value() {
        public float get(Actor context) {
            if (context instanceof Layout) {
                return ((Layout) context).getMaxHeight();
            }
            return context == null ? 0.0f : context.getHeight();
        }
    };
    public static Value maxWidth = new Value() {
        public float get(Actor context) {
            if (context instanceof Layout) {
                return ((Layout) context).getMaxWidth();
            }
            return context == null ? 0.0f : context.getWidth();
        }
    };
    public static Value minHeight = new Value() {
        public float get(Actor context) {
            if (context instanceof Layout) {
                return ((Layout) context).getMinHeight();
            }
            return context == null ? 0.0f : context.getHeight();
        }
    };
    public static Value minWidth = new Value() {
        public float get(Actor context) {
            if (context instanceof Layout) {
                return ((Layout) context).getMinWidth();
            }
            return context == null ? 0.0f : context.getWidth();
        }
    };
    public static Value prefHeight = new Value() {
        public float get(Actor context) {
            if (context instanceof Layout) {
                return ((Layout) context).getPrefHeight();
            }
            return context == null ? 0.0f : context.getHeight();
        }
    };
    public static Value prefWidth = new Value() {
        public float get(Actor context) {
            if (context instanceof Layout) {
                return ((Layout) context).getPrefWidth();
            }
            return context == null ? 0.0f : context.getWidth();
        }
    };
    public static final Fixed zero = new Fixed(0.0f);

    public static class Fixed extends Value {
        private final float value;

        public Fixed(float value) {
            this.value = value;
        }

        public float get(Actor context) {
            return this.value;
        }
    }

    public abstract float get(Actor actor);

    public static Value percentWidth(final float percent) {
        return new Value() {
            public float get(Actor actor) {
                return actor.getWidth() * percent;
            }
        };
    }

    public static Value percentHeight(final float percent) {
        return new Value() {
            public float get(Actor actor) {
                return actor.getHeight() * percent;
            }
        };
    }

    public static Value percentWidth(final float percent, final Actor actor) {
        if (actor != null) {
            return new Value() {
                public float get(Actor context) {
                    return actor.getWidth() * percent;
                }
            };
        }
        throw new IllegalArgumentException("actor cannot be null.");
    }

    public static Value percentHeight(final float percent, final Actor actor) {
        if (actor != null) {
            return new Value() {
                public float get(Actor context) {
                    return actor.getHeight() * percent;
                }
            };
        }
        throw new IllegalArgumentException("actor cannot be null.");
    }
}
