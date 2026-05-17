import { JoinLabelsPipe } from './join-labels.pipe';

describe('JoinLabelsPipe', () => {
  const pipe = new JoinLabelsPipe();

  it('returns empty string for null / empty input', () => {
    expect(pipe.transform(null)).toBe('');
    expect(pipe.transform(undefined)).toBe('');
    expect(pipe.transform([])).toBe('');
  });

  it('joins labelFr with comma by default', () => {
    expect(
      pipe.transform([
        { id: 1, code: 'A', labelFr: 'Alpha', sortOrder: 0 },
        { id: 2, code: 'B', labelFr: 'Beta', sortOrder: 0 },
      ]),
    ).toBe('Alpha, Beta');
  });

  it('respects custom separator', () => {
    expect(
      pipe.transform(
        [
          { id: 1, code: 'A', labelFr: 'Alpha', sortOrder: 0 },
          { id: 2, code: 'B', labelFr: 'Beta', sortOrder: 0 },
        ],
        ' | ',
      ),
    ).toBe('Alpha | Beta');
  });
});

